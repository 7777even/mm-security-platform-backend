package com.sinopec.mmsecurity.common.mask;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MaskingSerializer 纯单元测试（0% → 全覆盖）。
 * 验证：序列化出口按脱敏类型调用 MaskUtil.mask；createContextual 从 @Masked 注解读取类型。
 */
@ExtendWith(MockitoExtension.class)
class MaskingSerializerTest {

    @Mock
    JsonGenerator gen;
    @Mock
    SerializerProvider prov;

    @Test
    void serialize_defaultTypeNone_writesOriginalValue() throws Exception {
        MaskingSerializer s = new MaskingSerializer();
        s.serialize("13812345678", gen, prov);
        verify(gen).writeString("13812345678");
    }

    @Test
    void serialize_nullValue_writesNull() throws Exception {
        MaskingSerializer s = new MaskingSerializer();
        s.serialize(null, gen, prov);
        verify(gen).writeString((String) null);
    }

    @Test
    void createContextual_withMaskedAnnotation_masksByType() throws Exception {
        Masked masked = mock(Masked.class);
        when(masked.value()).thenReturn(MaskType.PHONE);
        BeanProperty property = mock(BeanProperty.class);
        when(property.getAnnotation(Masked.class)).thenReturn(masked);

        MaskingSerializer typed = (MaskingSerializer) new MaskingSerializer().createContextual(prov, property);
        typed.serialize("13812345678", gen, prov);
        verify(gen).writeString("138****5678");
    }

    @Test
    void createContextual_withoutAnnotation_returnsSameInstance() {
        MaskingSerializer base = new MaskingSerializer();
        assertSame(base, base.createContextual(prov, null));
    }
}
