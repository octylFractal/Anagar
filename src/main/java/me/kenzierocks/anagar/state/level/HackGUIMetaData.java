package me.kenzierocks.anagar.state.level;

import static com.google.common.base.Preconditions.checkState;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class HackGUIMetaData {

    public static final HackGUIMetaData.Builder builder() {
        return new AutoValue_HackGUIMetaData.Builder();
    }

    @AutoValue.Builder
    public static abstract class Builder {

        Builder() {
        }

        public abstract Builder title(String title);

        public abstract Builder moneyProvided(int moneyProvided);

        public abstract Builder processingPower(int processingPower);

        public abstract Builder stability(int stability);

        abstract HackGUIMetaData autoBuild();

        public HackGUIMetaData build() {
            HackGUIMetaData built = autoBuild();
            checkState(built.getMoneyProvided() > 0,
                    "moneyProvided must be > 0");
            checkState(built.getProcessingPower() > 0,
                    "processingPower must be > 0");
            return built;
        }

    }

    public abstract String getTitle();

    public abstract int getMoneyProvided();

    public abstract int getProcessingPower();

    public abstract int getStability();

}
