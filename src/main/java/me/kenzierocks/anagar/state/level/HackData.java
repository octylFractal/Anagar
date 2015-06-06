package me.kenzierocks.anagar.state.level;

import static com.google.common.base.Preconditions.checkState;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class HackData {

    public static final HackData.Builder builder() {
        return new AutoValue_HackData.Builder();
    }

    @AutoValue.Builder
    public static abstract class Builder {

        Builder() {
        }

        public abstract Builder title(String title);

        public abstract Builder moneyProvided(int moneyProvided);

        public abstract Builder processingPower(int processingPower);

        public abstract Builder stability(int stability);

        abstract HackData autoBuild();

        public HackData build() {
            HackData built = autoBuild();
            checkState(built.getMoneyProvided() > 0,
                       "moneyProvided must be > 0");
            checkState(built.getProcessingPower() > 0,
                       "processingPower must be > 0");
            checkState(0 <= built.getStability() && built.getStability() <= 100,
                       "stability must be a valid percentage (0-100)");
            return built;
        }

    }

    public abstract String getTitle();

    public abstract int getMoneyProvided();

    public abstract int getProcessingPower();

    public abstract int getStability();

    public HackData copy() {
        return new AutoValue_HackData.Builder(this).build();
    }

}
