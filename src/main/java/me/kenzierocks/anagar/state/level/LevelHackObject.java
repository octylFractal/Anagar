package me.kenzierocks.anagar.state.level;

import java.awt.Image;
import java.util.List;

import me.kenzierocks.anagar.Utility.ImageIO;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

enum LevelHackObject {

    BASIC_SERVER(HackData.builder().title("Basic Server").moneyProvided(20)
            .processingPower(2).stability(70).build()), END_USER(HackData
            .builder().title("End User").moneyProvided(5).processingPower(2)
            .stability(95).build()), PRO_SERVER(HackData.builder()
            .title("Professional Server Rack").moneyProvided(40)
            .processingPower(16).stability(50).build()), OTHER_PLAYER(null);

    public static final List<LevelHackObject> DEFAULTS = ImmutableList
            .of(BASIC_SERVER, END_USER, PRO_SERVER);

    private final Image image;
    private final Optional<HackData> metaData;

    private LevelHackObject(HackData metaData) {
        this.image = ImageIO.load(name().toLowerCase().replace("_", ""));
        this.metaData = Optional.fromNullable(metaData);
    }

    public Image getImage() {
        return this.image;
    }

    public Optional<HackData> getMetaData() {
        if (this.metaData.isPresent()) {
            return Optional.of(this.metaData.get().copy());
        }
        return Optional.absent();
    }

}
