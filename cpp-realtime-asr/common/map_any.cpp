//
// Created by fu on 11/19/19.
//

#include "map_any.h"
namespace realtime_asr {
std::map<std::string, boost::any> create_map() {
    return std::map<std::string, boost::any>();
}

bool is_int(const boost::any &operand) {
    return operand.type() == typeid(int);
}

bool is_char_ptr(const boost::any &operand) {
    try {
        boost::any_cast<const char *>(operand);
        return true;
    }
    catch (const boost::bad_any_cast &) {
        return false;
    }
}

bool is_string(const boost::any &operand) {
    return boost::any_cast<std::string>(&operand);
}

bool is_anymap(const boost::any &operand) {
    return boost::any_cast<std::map<std::string, boost::any>>(&operand);
}

}