#include <iostream>
#include <memory>
#include <string>
#include <boost/exception/all.hpp>
#include "common/log.h"
#include "session.h"

#ifdef WITH_SSL // 开启wss://支持

#include "common/root_certificates.h"
#endif
/* 异常信息的类型 */
typedef boost::error_info<struct tag_err_no, int> err_no;

const std::string DEFAULT_FILENAME = "pcm/16k-0.pcm";

namespace realtime_asr {

/**
 * 运行
 * @param filename 文件名。默认是 DEFAULT_FILENAME
 */
static void run(const std::string &filename) {
    net::io_context ioc;
    std::ifstream f{filename, std::ios::binary};

#ifdef WITH_SSL
    ssl::context ctx{ssl::context::tlsv12_client};
    load_root_certificates(ctx);
    std::make_shared<Session>(ioc, f,ctx)->run();
#else
    std::make_shared<Session>(ioc, f)->run();
#endif

    ioc.run();
    f.close();
}
}


int main(int argc, char *argv[]) {
    std::cout << "begin asr demo " << std::endl;
    std::string filename;
    if (argc >= 2) {
        filename = argv[1];
    } else {
        filename = DEFAULT_FILENAME;
    }
    try {
        realtime_asr::init_log(argc, argv);
        realtime_asr::run(filename);
    } catch (boost::exception &e) {
        LOG(ERROR) << *boost::get_error_info<err_no>(e);
    } catch (std::exception &e) {
        LOG(ERROR) << e.what();
    }
    LOG(INFO) << "FINISHED";
    return 0;
}
