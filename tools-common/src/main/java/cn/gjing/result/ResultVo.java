package cn.gjing.result;

import cn.gjing.enums.HttpStatus;
import lombok.*;
import cn.gjing.ParamUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Gjing
 * 返回模板
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultVo<T> {

    private Integer code;
    private String message;
    private T data;

    public static ResultVo success(String message, Object data) {
        return ResultVo.builder().code(HttpStatus.OK.getCode()).message(message).data(data).build();
    }

    public static ResultVo success() {
        return ResultVo.builder().code(HttpStatus.OK.getCode()).message(HttpStatus.OK.getMsg()).build();
    }

    public static ResultVo success(Object data) {
        return ResultVo.builder().code(HttpStatus.OK.getCode()).message(HttpStatus.OK.getMsg()).data(data).build();
    }

    public static ResultVo error(Integer code, String message) {
        return ResultVo.builder().code(code).message(message).build();
    }

    public static ResultVo error() {
        return ResultVo.builder().code(HttpStatus.BAD_REQUEST.getCode()).message(HttpStatus.BAD_REQUEST.getMsg()).build();
    }

    public static ResultVo error(String message) {
        return ResultVo.builder().code(HttpStatus.BAD_REQUEST.getCode()).message(message).build();
    }

    /**
     * 用户自定义返回,一般用于返回多个数据
     *
     * @param keys key
     * @param val  value
     * @return map
     */
    @Deprecated
    public static Map<String, Object> find(List<String> keys, List<Object> val) {
        Map<String, Object> map = new HashMap<>(16);
        for (int i = 0; i < keys.size(); i++) {
            map.put(keys.get(i), val.get(i));
        }
        return map;
    }

    /**
     * 一般用于登录
     * @param isLogin 非空则为登录
     * @param keys 还要返回的其他参数
     * @param values 参数对应的value
     * @return map
     */
    @Deprecated
    public static Map<String, Object> login(String isLogin,List<String> keys, List<Object> values) {
        Map<String, Object> map = new HashMap<>(10);
        if (ParamUtil.isNotEmpty(isLogin)) {
            map.put("code", HttpStatus.OK.getCode());
            map.put("message", HttpStatus.OK.getMsg());
        }else {
            map.put("code", HttpStatus.NO_LOGIN.getCode());
            map.put("message", HttpStatus.NO_LOGIN.getMsg());
        }
        if (keys != null && values != null) {
            for (int i = 0; i < keys.size(); i++) {
                map.put(keys.get(i), values.get(i));
            }
        }
        return map;
    }

}