//
// Created by fu on 11/19/19.
//

#include "json_writer.h"

#include "rapidjson/document.h"

#include "map_any.h"

namespace realtime_asr {

JsonWriter::JsonWriter() : _buffer(), _writer(_buffer) {
}

std::string JsonWriter::to_string(std::map<std::string, boost::any> map) {
    JsonWriter writer;
    writer.write(map);
    return writer.to_string();
}

void JsonWriter::write(const std::string &key, const std::string &value) {
    _writer.Key(key.c_str());
    _writer.String(value.c_str());
}

void JsonWriter::write(const std::string &key, int value) {
    _writer.Key(key.c_str());
    _writer.Int(value);
}

void JsonWriter::write(const std::map<std::string, boost::any> &map) {
    start_object();
    for (auto iter: map) {
        const std::string &key = iter.first;
        boost::any &value = iter.second;
        if (is_int(value)) {
            write(key, boost::any_cast<int>(value));
        } else if (is_char_ptr(value)) {
            write(key, boost::any_cast<const char *>(value));
        } else if (is_string(value)) {
            write(key, boost::any_cast<const std::string>(value));
        } else if (is_anymap(value)) {
            _writer.Key(key.c_str());
            write(boost::any_cast<const std::map<std::string, boost::any>>(value));
        }
    }
    end_object();
}

void JsonWriter::start_object() {
    _writer.StartObject();
}

void JsonWriter::start_object(const std::string &key) {
    _writer.Key(key.c_str());
    _writer.StartObject();
}

void JsonWriter::end_object() {
    _writer.EndObject();
}

std::string JsonWriter::to_string() {
    return _buffer.GetString();
}

}