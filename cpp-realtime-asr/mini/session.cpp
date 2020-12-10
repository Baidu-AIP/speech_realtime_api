//
// Created by fu on 11/17/19.
//

#include "session.h"
// 生产uuid
#include <boost/uuid/uuid_generators.hpp>
#include <boost/uuid/uuid_io.hpp>


namespace realtime_asr {

// ec 不为0表示有错误发送
#define CHECK_EC_RETURN(ec, what) do {\
    if (ec){\
        LOG(ERROR) << what << ": " << ec.value() << ": " << ec.message();\
        return;\
    }\
}while(0);

/**
 * 发送音频数据及收到识别结果
 * 1. 连接
 * 1.1 DNS解析
 * 1.2 连接端口
 * 1.3 websocket升级握手
 * 2. 连接成功后发送数据
 * 2.1 发送开始参数帧
 * 2.2 实时发送音频数据帧
 * 2.3 库接收识别结果
 * 2.4 发送结束帧
关闭连接
 */
#ifdef WITH_SSL
Session::Session(net::io_context &ioc, std::istream &stream, ssl::context &ctx)
        : resolver_(net::make_strand(ioc)), stream_(stream),
          read_buffer_(new char[5120]),
          ws_(net::make_strand(ioc), ctx) {

}
#else

Session::Session(net::io_context &ioc, std::istream &stream)
        : resolver_{net::make_strand(ioc)}, stream_{stream},
          read_buffer_{new char[5120]},
          ws_{net::make_strand(ioc)} {

}

#endif

/**
 * 开始运行
 */
void Session::run() {
    // 1.1 DNS解析，解析后调用on_resolve
    LOG(INFO) << "try to resolve: " << HOST << ":" << PORT;
    resolver_.async_resolve(HOST, std::to_string(PORT),
                            beast::bind_front_handler(&Session::on_resolve, shared_from_this()));
}

/**
 *
 * 1.2 连接端口， 回调on_connect
 * @param ec
 * @param results
 */
void Session::on_resolve(beast::error_code ec, tcp::resolver::results_type results) {
    CHECK_EC_RETURN(ec, "resolve");

    // Set the timeout for the operation
    beast::get_lowest_layer(ws_).expires_after(std::chrono::milliseconds(2000));

    // Make the connection on the IP address we get from a lookup
    beast::get_lowest_layer(ws_).async_connect(results,
                                               beast::bind_front_handler(&Session::on_connect, shared_from_this()));
}

/**
 * 1.3 websocket升级握手，回调on_handshake
 *
 * @param ec
 */
void Session::on_connect(beast::error_code ec, tcp::resolver::results_type::endpoint_type) {
    CHECK_EC_RETURN(ec, "connect");
#ifdef WITH_SSL
    ws_.next_layer().async_handshake(ssl::stream_base::client, beast::bind_front_handler(
            &Session::on_ssl_handshake, shared_from_this()));
#else
    handshake();
#endif

}

void Session::handshake() {
    // Turn off the timeout on the tcp_stream, because
    // the websocket stream has its own timeout system.
    beast::get_lowest_layer(ws_).expires_never();

    // 设置超时参数
    websocket::stream_base::timeout opt{
            std::chrono::milliseconds(2000),   // handshake timeout
            std::chrono::milliseconds(10000),        // idle timeout
            false
    }; // if(ec == beast::error::timeout)
    ws_.set_option(opt);

    // Perform the websocket handshake
    std::string path = PATH + "?sn=" + gene_uuid();
    LOG(INFO) << "URI: " << HOST << path;
    ws_.async_handshake(HOST, path, beast::bind_front_handler(&Session::on_handshake, shared_from_this()));
}

#ifdef WITH_SSL
void Session::on_ssl_handshake(beast::error_code ec) {
    CHECK_EC_RETURN(ec, "on_ssl_handshake");
    handshake();
}
#endif

/**
 * 2.1 发送开始参数帧， 回调on_write
 *
 * @param ec
 */
void Session::on_handshake(beast::error_code ec) {
    CHECK_EC_RETURN(ec, "handshake");

    // 接收回调
    ws_.async_read(buffer_, beast::bind_front_handler(
            &Session::on_read,
            shared_from_this()));
    std::string start_json = gene_start_params();
    LOG(INFO) << "try to send START Frame :" << start_json;
    ws_.async_write(net::buffer(start_json), beast::bind_front_handler(
            &Session::on_write, shared_from_this()));


}

/**
 * 2.2 实时发送音频数据帧，回调on_write
 * 2.4 音频流结束，发送结束帧，回调on_finish_write
 * @param ec
 * @param bytes_transferred
 */
void Session::on_write(beast::error_code ec, std::size_t bytes_transferred) {
    LOG(DEBUG) << bytes_transferred << " writen ";
    CHECK_EC_RETURN(ec, "write");
    if (stream_) {
        stream_.read(read_buffer_.get(), 5120);
        ws_.auto_fragment(false);
        ws_.binary(true);
        ws_.async_write(net::buffer(read_buffer_.get(), stream_.gcount()), beast::bind_front_handler(
                &Session::on_write, shared_from_this()));
        std::this_thread::sleep_for(std::chrono::milliseconds(160));
    } else {
        ws_.binary(false);
        std::string finish_json = gene_finish_params();
        LOG(INFO) << "try to send FINISH Frame :" << finish_json;
        ws_.async_write(net::buffer(finish_json), beast::bind_front_handler(
                &Session::on_finish_write, shared_from_this()));
    }
}

/**
 * 结束帧写回调
 *
 * @param ec
 * @param bytes_transferred
 */
void Session::on_finish_write(beast::error_code ec, std::size_t bytes_transferred) {
    LOG(INFO) << bytes_transferred << " writen ; last Frame sent";
    CHECK_EC_RETURN(ec, "write last Frame");
}


/**
 * 2.3 库接收识别结果， 回调on_read
 * @param ec
 * @param bytes_transferred
 */
void Session::on_read(beast::error_code ec, std::size_t bytes_transferred) {
    CHECK_EC_RETURN(ec, "read");
    LOG(INFO) << "recieve data size: " << bytes_transferred << "; " << beast::make_printable(buffer_.data());
    buffer_.consume(buffer_.size());
    ws_.async_read(buffer_, beast::bind_front_handler(
            &Session::on_read, shared_from_this()));
}

/**
 * 开始帧参数
 * @return
 */
std::string Session::gene_start_params() {
    auto data = create_map();
    data["appid"] = APPID;
    data["appkey"] = APPKEY;
    data["dev_pid"] = DEV_PID;
    data["cuid"] = "your_self_defined_user_id";
    data["format"] = "pcm";
    data["sample"] = 16000;
    auto params = create_map();
    params["type"] = "START";
    params["data"] = data;
    return JsonWriter::to_string(params);
}

/**
 * 结束帧参数
 * @return
 */
std::string Session::gene_finish_params() {
    auto params = create_map();
    params["type"] = "FINISH";
    return JsonWriter::to_string(params);
}

/**
 * 生成uuid
 * @return
 */
std::string Session::gene_uuid() {
    boost::uuids::uuid a_uuid = boost::uuids::random_generator()();
    return boost::uuids::to_string(a_uuid);
}

}