package com.sinopec.mmsecurity.common.mask;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import java.io.IOException;

/**
 * Jackson 序列化期脱敏器。配合 {@link Masked} 注解使用，从字段注解读取脱敏类型后调用 {@link MaskUtil}。
 *
 * <p>仅作用于响应出口（序列化阶段），不影响数据库存储值与业务逻辑。未知类型或缺注解除原样输出。</p>
 */
public class MaskingSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private MaskType type = MaskType.NONE;

    public MaskingSerializer() {
    }

    private MaskingSerializer(MaskType type) {
        this.type = type;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(MaskUtil.mask(type, value));
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
        if (property != null) {
            Masked masked = property.getAnnotation(Masked.class);
            if (masked != null) {
                return new MaskingSerializer(masked.value());
            }
        }
        return this;
    }
}
