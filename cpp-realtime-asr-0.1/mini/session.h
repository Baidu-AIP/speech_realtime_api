//
// Created by fu on 11/17/19.
//

#ifndef REALTIME_ASR_SESSION_H
#define REALTIME_ASR_SESSION_H

#include <cstdlib>
#include <functional>
#include <iostream>
#include <memory>
#include <string>
#include <thread>
#include <chrono>


#include <boost/beast/core.hpp>
#include <boost/beast/websocket.hpp>
#include <boost/asio/strand.hpp>
#ifdef WITH_SSL
#include <boost/beast/ssl.hpp>
#include <boost/beast/websocket/ssl.hpp>
#endif
#include "const.h"
#include "common/log.h"
#include "common/json_writer.h"
#include "common/map_any.h"


namespace realtime_asr {
namespace beast = boost::beast;         // from <boost/beast.hpp>
namespace websocket = beast::websocket; // from <boost/beast/websocket.hpp>
namespace net = boost::asio;            // from <boost/asio.hpp>
namespace ssl = boost::asio::ssl;       // from <boost/asio/ssl.hpp>
using tcp = boost::asio::ip::tcp;       // from <boost/asio/ip/tcp.hpp>
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
class Session : public std::enable_shared_from_this<Session> {


public:

#ifdef WITH_SSL
    // Resolver and socket require an io_context
    explicit Session(net::io_context &ioc, std::istream &stream, ssl::context &ctx);
#else

    explicit Session(net::io_context &ioc, std::istream &stream);

#endif

    // 1.1 DNS解析
    void run();


private:
    tcp::resolver resolver_;
#ifdef WITH_SSL
    websocket::stream<beast::ssl_stream<beast::tcp_stream>> ws_;
#else
    websocket::stream<beast::tcp_stream> ws_;
#endif
    beast::flat_buffer buffer_;
    std::unique_ptr<char> read_buffer_;
    std::istream &stream_;

    // 1.2 DNS解析成功回调， 并开始连接端口
    void on_resolve(beast::error_code ec, tcp::resolver::results_type results);

    // 1.3 连接端口成功回调, 开始websocket握手
    void on_connect(beast::error_code ec, tcp::resolver::results_type::endpoint_type);

#ifdef WITH_SSL
    void on_ssl_handshake(beast::error_code ec);
#endif

    void handshake();

    // 2.1 websocket升级握手成功， 发送开始参数帧
    void on_handshake(beast::error_code ec);

    // 2.2 开始参数帧成功，发送音频数据，并回调这个方法
    // 2.4 音频数据发送完，发送结束帧
    void on_write(beast::error_code ec, std::size_t bytes_transferred);

    // 结束帧发送成功
    void on_finish_write(beast::error_code ec, std::size_t bytes_transferred);

    // 库接收识别结果
    void on_read(beast::error_code ec, std::size_t bytes_transferred);

    // 开始参数帧
    std::string gene_start_params();

    // 结束参数帧
    std::string gene_finish_params();

    // 生产uuid
    std::string gene_uuid();
};
}

#endif //REALTIME_ASR_SESSION_H
