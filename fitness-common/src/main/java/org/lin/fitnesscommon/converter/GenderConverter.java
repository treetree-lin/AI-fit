package org.lin.fitnesscommon.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.lin.fitnesscommon.entity.UserProfile.Gender;

@Converter
public class GenderConverter implements AttributeConverter<Gender, String> {

    @Override
    public String convertToDatabaseColumn(Gender attribute) {
        if (attribute == null) return null;
        // 存储中文到数据库
        return attribute.getChineseName();
    }

    @Override
    public Gender convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return null;
        // 兼容：如果数据库里已经是中文，则使用 fromChineseName；
        // 如果是英文名称（MALE/FEMALE/OTHER），尝试直接通过 valueOf
        try {
            // 优先尝试按中文解析
            return Gender.fromChineseName(dbData);
        } catch (IllegalArgumentException ex) {
            // fallback: 尝试英文值
            try {
                return Gender.valueOf(dbData);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
