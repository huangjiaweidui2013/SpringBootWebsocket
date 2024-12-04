package org.lang.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AjaxResult<T> {

    private Integer code;

    private String msg;

    private T data;

    public static <V> AjaxResult<V> success() {
        return AjaxResult.success(null);
    }

    public static <V> AjaxResult<V> success(V data) {
        return AjaxResult.success("success", data);
    }

    public static <V> AjaxResult<V> success(String msg, V data) {
        return new AjaxResult<>(0, msg, data);
    }

    public static <V> AjaxResult<V> success(int code, String msg, V data) {
        return new AjaxResult<>(code, msg, data);
    }

    public static <V> AjaxResult<V> error() {
        return AjaxResult.error(null);
    }

    public static <V> AjaxResult<V> error(V data) {
        return AjaxResult.error("fail", data);
    }

    public static <V> AjaxResult<V> error(String msg, V data) {
        return new AjaxResult<>(1, msg, data);
    }

    public static <V> AjaxResult<V> error(int code, String msg, V data) {
        return new AjaxResult<>(code, msg, data);
    }
}
