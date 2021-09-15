package eu.okaeri.platform.minecraft.i18n;

import lombok.Data;

@Data(staticConstructor = "of")
public class I18nMessageColors {
    private final String messageColor;
    private final String fieldsColor;
}
