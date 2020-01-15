//
// Created by fu on 11/19/19.
//

#ifndef REALTIME_ASR_JSON_WRITER_H
#define REALTIME_ASR_JSON_WRITER_H

#include <string>
#include <map>
#include <rapidjson/writer.h>
#include <boost/any.hpp>

namespace realtime_asr {
/**
 * 简单的Json 写封装
 */
class JsonWriter {
public:
    JsonWriter();

    /**
     * 将anymap转为json
     * @param map
     * @return
     */
    static std::string to_string(std::map<std::string, boost::any> map);

    void write(const std::string& key, const std::string& value);

    void write(const std::string& key, int value);

    void write(const std::map<std::string, boost::any>& map);

    void start_object();

    void start_object(const std::string &key);

    void end_object();

    std::string to_string();

private:
    rapidjson::StringBuffer _buffer;
    rapidjson::Writer<rapidjson::StringBuffer> _writer;

};

}
#endif //REALTIME_ASR_JSON_WRITER_H
