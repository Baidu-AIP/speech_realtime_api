//
// Created by fu on 11/19/19.
//

#ifndef REALTIME_ASR_MAP_ANY_H
#define REALTIME_ASR_MAP_ANY_H

#include <map>
#include <string>
#include <boost/any.hpp>

namespace realtime_asr {
/**
 * 新建map， 目前只支持 整数，字符串，及map作为value
 * @return
 */
std::map<std::string, boost::any> create_map();

/**
 * value 是否是int
 *
 * @param operand
 * @return
 */
bool is_int(const boost::any &operand);

/**
 * 是否是 char 指针
 * @param operand
 * @return
 */
bool is_char_ptr(const boost::any &operand);

/**
 * 是否是string
 * @param operand
 * @return
 */
bool is_string(const boost::any &operand);

/**
 * 是否是map
 * @param operand
 * @return
 */
bool is_anymap(const boost::any &operand);
}

#endif //REALTIME_ASR_MAP_ANY_H
