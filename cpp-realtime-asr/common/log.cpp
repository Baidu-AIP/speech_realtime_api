//
// Created by fu on 11/27/19.
//
#include "log.h"


INITIALIZE_EASYLOGGINGPP

namespace realtime_asr {

/**
 * 初始化日志
 *
 * @param argc
 * @param argv
 */
void init_log(int argc, char *argv[]) {
    START_EASYLOGGINGPP(argc, argv);
    el::Configurations defaultConf;
    defaultConf.setToDefault();
    defaultConf.setGlobally(el::ConfigurationType::SubsecondPrecision, "4");
    defaultConf.setGlobally(el::ConfigurationType::Format,
                            "[%datetime{%H:%m:%s.%g}][%level][ %file:%line ][%thread] %msg");
    el::Loggers::reconfigureLogger("default", defaultConf);
    LOG(INFO) << "Logger inited";
}
}