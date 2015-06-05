package me.kenzierocks.anagar.state;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.auto.value.AutoValue;

public interface StateType {

    @AutoValue
    abstract class BaseStateType implements StateType {

        public static final BaseStateType.Builder builder() {
            return new AutoValue_StateType_BaseStateType.Builder();
        }

        @AutoValue.Builder
        public static abstract class Builder {

            Builder() {
            }

            public Builder idAndDiscriminator(String idDsc) {
                return this.id(idDsc).discriminator(idDsc);
            }

            public abstract Builder id(String id);

            public abstract Builder discriminator(String dsc);

            public abstract BaseStateType build();

        }

        BaseStateType() {
        }

        public Builder toBuilder() {
            return new AutoValue_StateType_BaseStateType.Builder(this);
        }

    }

    enum Defaults {

        MAIN(BaseStateType.builder().idAndDiscriminator("main").build()), LEVEL(
                BaseStateType.builder().id("level").discriminator("none")
                        .build()), PAUSE(BaseStateType.builder().id("pause")
                .discriminator("none").build());

        private final BaseStateType stateType;

        private Defaults(BaseStateType type) {
            this.stateType = type;
        }

        public StateType getAsIs() {
            return this.stateType;
        }

        public StateType createStateType(String dsc) {
            checkNotNull(dsc);
            return this.stateType.toBuilder().discriminator(dsc).build();
        }

    }

    String getId();

    String getDiscriminator();

}
