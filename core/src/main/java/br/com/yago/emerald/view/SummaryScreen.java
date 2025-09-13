package br.com.yago.emerald.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import br.com.yago.emerald.EmeraldGame;
import br.com.yago.emerald.assets.AnimationLoader;
import br.com.yago.emerald.assets.AssetsLoader;
import br.com.yago.emerald.model.Ability;
import br.com.yago.emerald.model.ExperienceCalculator;
import br.com.yago.emerald.model.Item;
import br.com.yago.emerald.model.Move;
import br.com.yago.emerald.model.Party;
import br.com.yago.emerald.model.Pokeball;
import br.com.yago.emerald.model.Pokemon;
import br.com.yago.emerald.model.Pokemon.Gender;
import br.com.yago.emerald.model.PokemonNature;
import br.com.yago.emerald.model.PokemonType;

public class SummaryScreen extends ScreenAdapter {

    private final EmeraldGame game;
    private final SpriteBatch batch;
    private final Viewport viewport;
    private final OrthographicCamera camera;
    private final ShapeRenderer shapeRenderer;

    private final BitmapFont whiteFont;
    private final BitmapFont blackFont;
    private final BitmapFont increasedStatFont;
    private final BitmapFont decreasedStatFont;
    private final GlyphLayout layout = new GlyphLayout();

    private final TextureAtlas typeAtlas;
    private final TextureAtlas pokeballsAtlas;
    private final TextureAtlas partyUiAtlas;
    private final TextureAtlas summaryAtlas;

    private final TextureRegion infoBackground;
    private final TextureRegion skillsBackground;
    private final TextureRegion movesBackground;
    private final TextureRegion infoGuide, skillsGuide, movesGuide;
    private final TextureRegion moveSelectedOverlay;

    private final Party party;
    private int currentIndex;
    private Pokemon currentPokemon;
    private Animation<TextureRegion> pokemonAnimation;
    private float stateTimer = 0f;

    private enum SummaryState { POKEMON_INFO, POKEMON_SKILLS, POKEMON_MOVES }
    private SummaryState currentState = SummaryState.POKEMON_INFO;
    private SummaryState previousState;

    private enum AnimationType { NONE, SLIDING }
    private AnimationType animationType = AnimationType.NONE;
    private float animationTimer = 0f;
    private static final float SLIDE_DURATION = 0.25f;

    private int selectedMoveIndex = 0;
    private float blinkTimer = 0f;
    private boolean showBlink = true;

    public SummaryScreen(EmeraldGame game, SpriteBatch batch, AssetsLoader assetLoader, Party party, int initialIndex) {
        this.game = game;
        this.batch = batch;
        this.party = party;
        this.camera = new OrthographicCamera();
        this.viewport = new ExtendViewport(240, 160, camera);
        this.shapeRenderer = new ShapeRenderer();

        this.infoBackground = new TextureRegion(assetLoader.manager.get(assetLoader.SUMMARY_UI, Texture.class));
        this.whiteFont = assetLoader.manager.get(assetLoader.PARTY_FONT_NAME + ".otf", BitmapFont.class);
        this.blackFont = assetLoader.manager.get(assetLoader.BAG_FONT_NAME + ".otf", BitmapFont.class);
        this.increasedStatFont = assetLoader.manager.get(assetLoader.INCREASED_STAT_FONT_NAME + ".otf", BitmapFont.class);
        this.decreasedStatFont = assetLoader.manager.get(assetLoader.DECREASED_STAT_FONT_NAME + ".otf", BitmapFont.class);

        this.typeAtlas = assetLoader.manager.get(assetLoader.TYPE_ATLAS, TextureAtlas.class);
        this.pokeballsAtlas = assetLoader.manager.get(assetLoader.POKEBALLS_ATLAS, TextureAtlas.class);
        this.partyUiAtlas = assetLoader.manager.get(assetLoader.PARTY_UI_ATLAS, TextureAtlas.class);

        this.summaryAtlas = assetLoader.manager.get(assetLoader.SUMMARY_ATLAS, TextureAtlas.class);
        this.infoGuide = summaryAtlas.findRegion("info");
        this.skillsGuide = summaryAtlas.findRegion("skills");
        this.movesGuide = summaryAtlas.findRegion("moves");
        this.skillsBackground = summaryAtlas.findRegion("skills_screen");
        this.movesBackground = summaryAtlas.findRegion("moves_screen");
        this.moveSelectedOverlay = summaryAtlas.findRegion("move_selected");

        loadPokemon(initialIndex);
    }

    private void loadPokemon(int index) {
        if (pokemonAnimation != null) {
            for (Object frameObject : pokemonAnimation.getKeyFrames()) {
                TextureRegion frame = (TextureRegion) frameObject;
                if (frame.getTexture() != null) {
                    frame.getTexture().dispose();
                }
            }
        }

        this.currentIndex = index;
        this.currentPokemon = party.getPokemon(currentIndex);
        this.pokemonAnimation = AnimationLoader.loadGifAnimation(currentPokemon.getFrontSpriteGifPath());
        this.stateTimer = 0f;
        this.selectedMoveIndex = 0;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (animationType != AnimationType.NONE) return true;

                if (keycode == Input.Keys.X || keycode == Input.Keys.ESCAPE) {
                    game.closeSummaryScreen();
                    return true;
                }

                if (currentState == SummaryState.POKEMON_MOVES) {
                    if (keycode == Input.Keys.DOWN) {
                        selectedMoveIndex = (selectedMoveIndex + 1) % currentPokemon.getMoves().size;
                        return true;
                    }
                    if (keycode == Input.Keys.UP) {
                        selectedMoveIndex = (selectedMoveIndex - 1 + currentPokemon.getMoves().size) % currentPokemon.getMoves().size;
                        return true;
                    }
                }

                if (keycode == Input.Keys.DOWN) {
                    int nextIndex = (currentIndex + 1) % party.getPartySize();
                    loadPokemon(nextIndex);
                    return true;
                }
                if (keycode == Input.Keys.UP) {
                    int prevIndex = (currentIndex - 1 + party.getPartySize()) % party.getPartySize();
                    loadPokemon(prevIndex);
                    return true;
                }
                if (keycode == Input.Keys.RIGHT) {
                    changeTab(true);
                    return true;
                }
                if (keycode == Input.Keys.LEFT) {
                    changeTab(false);
                    return true;
                }
                return false;
            }
        });
    }

    private void changeTab(boolean next) {
        previousState = currentState;
        int numStates = SummaryState.values().length;
        int nextIndex = next ? (currentState.ordinal() + 1) % numStates : (currentState.ordinal() - 1 + numStates) % numStates;
        currentState = SummaryState.values()[nextIndex];

        if (currentState == SummaryState.POKEMON_MOVES) {
            selectedMoveIndex = 0;
        }

        animationType = AnimationType.SLIDING;
        animationTimer = 0f;
    }

    private void update(float delta) {
        stateTimer += delta;
        if (animationType != AnimationType.NONE) {
            animationTimer += delta;
            if (animationTimer >= SLIDE_DURATION) {
                animationType = AnimationType.NONE;
            }
        }

        if (currentState == SummaryState.POKEMON_MOVES) {
            blinkTimer += delta;
            if (blinkTimer > 0.3f) {
                showBlink = !showBlink;
                blinkTimer = 0f;
            }
        }
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        batch.begin();

        batch.draw(infoBackground, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        drawPokemonInfoPanel();
        drawScreens();
        drawTabHeader();

        batch.end();
    }

    private void drawScreens() {
        if (animationType == AnimationType.NONE) {
            drawScreenForState(currentState, 0);
        } else {
            float progress = Math.min(1f, animationTimer / SLIDE_DURATION);
            float screenWidth = viewport.getWorldWidth();

            float currentOffset = Interpolation.pow2Out.apply(-screenWidth, 0, progress);
            float previousOffset = Interpolation.pow2Out.apply(0, screenWidth, progress);

            drawScreenForState(previousState, previousOffset);
            drawScreenForState(currentState, currentOffset);
        }
    }

    private void drawScreenForState(SummaryState state, float xOffset) {
        switch (state) {
            case POKEMON_INFO:
                drawInfoScreenContent(xOffset);
                break;
            case POKEMON_SKILLS:
                drawSkillsScreen(xOffset);
                break;
            case POKEMON_MOVES:
                drawMovesScreen(xOffset);
                break;
        }
    }

    private void drawTabHeader() {
        TextureRegion guide;
        String label;

        switch(currentState) {
            case POKEMON_SKILLS:
                guide = skillsGuide;
                label = "POKEMON SKILLS";
                break;
            case POKEMON_MOVES:
                guide = movesGuide;
                label = "POKEMON MOVES";
                break;
            default: // POKEMON_INFO
                guide = infoGuide;
                label = "POKEMON INFO";
                break;
        }

        if (guide != null) {
            float guideX = (viewport.getWorldWidth() - guide.getRegionWidth()) / 2;
            float guideY = viewport.getWorldHeight() - guide.getRegionHeight() ;
            batch.draw(guide, guideX, guideY);
        }

        if (label != null) {
            whiteFont.draw(batch, label, 2f, viewport.getWorldHeight() - 1f);
        }
    }

    private void drawPokemonInfoPanel() {
        if (pokemonAnimation != null) {
            TextureRegion currentFrame = pokemonAnimation.getKeyFrame(stateTimer, true);
            batch.draw(currentFrame, 10, 64, 64, 64);
        }
        drawPokeballIcon();
        String displayName = currentPokemon.getNickname() + "\n" + " / " + currentPokemon.getSpeciesName();
        whiteFont.draw(batch, displayName, 4, 64);
        drawLevelAndGender();
    }

    private void drawInfoScreenContent(float xOffset) {
        drawOriginalTrainerInfo(xOffset);
        drawPokemonTypes(xOffset);
        drawAbilityInfo(xOffset);
        drawMetInformation(xOffset);
    }

    private void drawSkillsScreen(float xOffset) {
        if (skillsBackground != null) {
            batch.draw(skillsBackground, xOffset, 0);
        }
    
        float leftMargin = xOffset + 85f;
        float topY = 126f;
    
        Item heldItem = currentPokemon.getHeldItem();
        String itemName = heldItem != null ? heldItem.getName().toUpperCase() : "NONE";
        blackFont.draw(batch, itemName, leftMargin + 1, topY);
    
        float statsYLeft = topY - 22f;
        float statsYRight = topY - 22f;
    
        float labelXLeft = xOffset + 85f;
        float valueXLeft = labelXLeft + 65f;
    
        float labelXRight = xOffset + 177f;
        float valueXRight = labelXRight + 40f;
    
        PokemonNature nature = currentPokemon.getNature();
    
        whiteFont.draw(batch, "HP", labelXLeft, statsYLeft);
        String hpStr = currentPokemon.getCurrentHP() + "/" + currentPokemon.getMaxHP();
        layout.setText(blackFont, hpStr);
        blackFont.draw(batch, hpStr, valueXLeft + 24 - layout.width, statsYLeft);
        statsYLeft -= 16;
    
        float attackMod = nature.getModifierForStat("ATTACK");
        whiteFont.draw(batch, "ATTACK", labelXLeft, statsYLeft);
        BitmapFont attackFont = blackFont;
        if (attackMod > 1.0f) attackFont = increasedStatFont;
        else if (attackMod < 1.0f) attackFont = decreasedStatFont;
        attackFont.draw(batch, String.valueOf(currentPokemon.getAttack()), valueXLeft, statsYLeft);
        statsYLeft -= 16;
    
        float defenseMod = nature.getModifierForStat("DEFENSE");
        whiteFont.draw(batch, "DEFENSE", labelXLeft, statsYLeft);
        BitmapFont defenseFont = blackFont;
        if (defenseMod > 1.0f) defenseFont = increasedStatFont;
        else if (defenseMod < 1.0f) defenseFont = decreasedStatFont;
        defenseFont.draw(batch, String.valueOf(currentPokemon.getDefense()), valueXLeft, statsYLeft);
    
        float spAttackMod = nature.getModifierForStat("SPECIAL_ATTACK");
        whiteFont.draw(batch, "SP. ATK", labelXRight, statsYRight);
        BitmapFont spAttackFont = blackFont;
        if (spAttackMod > 1.0f) spAttackFont = increasedStatFont;
        else if (spAttackMod < 1.0f) spAttackFont = decreasedStatFont;
        spAttackFont.draw(batch, String.valueOf(currentPokemon.getSpecialAttack()), valueXRight, statsYRight);
        statsYRight -= 16;
    
        float spDefenseMod = nature.getModifierForStat("SPECIAL_DEFENSE");
        whiteFont.draw(batch, "SP. DEF", labelXRight, statsYRight);
        BitmapFont spDefenseFont = blackFont;
        if (spDefenseMod > 1.0f) spDefenseFont = increasedStatFont;
        else if (spDefenseMod < 1.0f) spDefenseFont = decreasedStatFont;
        spDefenseFont.draw(batch, String.valueOf(currentPokemon.getSpecialDefense()), valueXRight, statsYRight);
        statsYRight -= 16;
    
        float speedMod = nature.getModifierForStat("SPEED");
        whiteFont.draw(batch, "SPEED", labelXRight, statsYRight);
        BitmapFont speedFont = blackFont;
        if (speedMod > 1.0f) speedFont = increasedStatFont;
        else if (speedMod < 1.0f) speedFont = decreasedStatFont;
        speedFont.draw(batch, String.valueOf(currentPokemon.getSpeed()), valueXRight, statsYRight);
    
        float expLabelX = xOffset + 85f;
        float expValueX = expLabelX + 65f;
        float expY = Math.min(statsYLeft, statsYRight) - 26f;
    
        whiteFont.draw(batch, "EXP. POINTS", expLabelX, expY);
        String expStr = String.valueOf(currentPokemon.getExperiencePoints());
        layout.setText(blackFont, expStr);
        blackFont.draw(batch, expStr, expValueX + 68 - layout.width, expY);
        expY -= 12;
    
        whiteFont.draw(batch, "TO NEXT LV.", expLabelX, expY);
        String nextLvStr = String.valueOf(currentPokemon.getExperienceToNextLevel());
        layout.setText(blackFont, nextLvStr);
        blackFont.draw(batch, nextLvStr, expValueX + 68 - layout.width, expY);

        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        float barWidth = 64f;
        float barHeight = 3f;
        float barX = (expValueX + 83) - barWidth -1;
        float barY = expY - 23;

        shapeRenderer.setColor(new Color(0f,0f,0f,0f));
        shapeRenderer.rect(barX, barY, barWidth, barHeight);

        int totalExpForCurrentLevel = ExperienceCalculator.getTotalExpForLevel(currentPokemon.getLevel(), currentPokemon.getExperienceGroup());
        int expInCurrentLevel = currentPokemon.getExperiencePoints() - totalExpForCurrentLevel;
        int expForNext = ExperienceCalculator.getExpToNextLevel(currentPokemon.getLevel(), currentPokemon.getExperienceGroup());

        float progressPercentage = 0f;
        if (expForNext > 0) {
            progressPercentage = (float) expInCurrentLevel / expForNext;
        }

        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.rect(barX, barY, barWidth * progressPercentage, barHeight);

        shapeRenderer.end();

        batch.begin();
    }

    private void drawMovesScreen(float xOffset) {
        if (movesBackground != null) batch.draw(movesBackground, xOffset, 0);

        Array<Move> moves = currentPokemon.getMoves();

        final float COL_LEFT     = xOffset + 85f;
        final float ROW_Y0       = 122f;
        final float ROW_STEP     = 16f;
        final float TYPE_W = 32f, TYPE_H = 12f;

        final float NAME_X       = COL_LEFT + TYPE_W + 1f;
        final float LIST_RIGHT   = xOffset + 238f;
        final float PP_PAD_RIGHT = 0f;

        final float EFFECT_LBL_X = xOffset + 4f;
        final float EFFECT_VAL_X = xOffset + 60f;
        final float EFFECT_Y0    = 36f;
        final float EFFECT_DY    = 16f;

        final float DESC_X     = COL_LEFT + 1f;
        final float DESC_TOP_Y = 36f;
        final float DESC_W     = (LIST_RIGHT - 8f) - DESC_X;

        for (int i = 0; i < moves.size; i++) {
            Move mv = moves.get(i);
            float y = ROW_Y0 - i * ROW_STEP;

            String ppText = "PP" + mv.getCurrentPP() + "/" + mv.getMaxPP();
            layout.setText(blackFont, ppText);
            float ppX = LIST_RIGHT - PP_PAD_RIGHT - layout.width;
            float rightEdge = ppX + layout.width;

            if (i == selectedMoveIndex && showBlink && moveSelectedOverlay != null) {
                final float SEL_PAD_L = -2f;
                final float SEL_PAD_R =  2f;
                final float SEL_TOP   = 13f;
                final float SEL_H     = 14f;

                float selX = COL_LEFT + SEL_PAD_L;
                float selY = y - SEL_TOP;
                float selW = (rightEdge - selX) + SEL_PAD_R;

                batch.draw(moveSelectedOverlay, selX, selY, selW, SEL_H);
            }

            TextureRegion tr = typeAtlas.findRegion(mv.getType().name().toLowerCase());
            if (tr != null) batch.draw(tr, COL_LEFT, y - 12f, TYPE_W, TYPE_H);

            whiteFont.draw(batch, mv.getName().toUpperCase(), NAME_X, y);

            blackFont.draw(batch, ppText, ppX, y);
        }

        if (moves.size > 0) {
            Move sel = moves.get(selectedMoveIndex);

            whiteFont.draw(batch, "POWER",    EFFECT_LBL_X, EFFECT_Y0);
            blackFont.draw(batch, sel.getPowerString(),     EFFECT_VAL_X, EFFECT_Y0);

            whiteFont.draw(batch, "ACCURACY", EFFECT_LBL_X, EFFECT_Y0 - EFFECT_DY);
            blackFont.draw(batch, sel.getAccuracyString(),  EFFECT_VAL_X, EFFECT_Y0 - EFFECT_DY);
            
            layout.setText(blackFont, sel.getDescription(), new Color(96f/255f,96f/255f,96f/255f,1f), DESC_W, Align.left, true);
            blackFont.draw(batch, layout, DESC_X, DESC_TOP_Y);
        }
    }

    private void drawPokeballIcon() {
        Pokeball pokeball = currentPokemon.getPokeball();
        if (pokeball != null) {
            TextureRegion pokeballRegion = pokeballsAtlas.findRegion(pokeball.name().toLowerCase());
            if (pokeballRegion != null) {
                batch.draw(pokeballRegion, 4, 18, 12, 12);
            }
        }
    }

    private void drawLevelAndGender() {
        String levelStr = "Lv" + currentPokemon.getLevel();
        whiteFont.draw(batch, levelStr, 20, 30);
        layout.setText(whiteFont, levelStr);
        float levelWidth = layout.width;
        Gender gender = currentPokemon.getGender();
        TextureRegion genderIcon = null;
        if (gender == Gender.MALE) {
            genderIcon = partyUiAtlas.findRegion("party_male");
        } else if (gender == Gender.FEMALE) {
            genderIcon = partyUiAtlas.findRegion("party_female");
        }
        if (genderIcon != null) {
            float iconX = 20 + levelWidth + 5;
            float iconY = 30 - layout.height + (layout.height - genderIcon.getRegionHeight()) / 2f;
            batch.draw(genderIcon, iconX, iconY);
        }
    }

    private void drawOriginalTrainerInfo(float xOffset) {
        float startX = 85f;
        float y = 125f;
        float currentX = startX;
        String otLabel = "OT /";
        layout.setText(blackFont, otLabel);
        blackFont.draw(batch, layout, xOffset + currentX, y);
        currentX += layout.width + 4;
        String trainerName = currentPokemon.getOriginalTrainerName();
        whiteFont.draw(batch, trainerName, xOffset + currentX, y);
    }

    private void drawPokemonTypes(float xOffset) {
        PokemonType type1 = currentPokemon.getType1();
        PokemonType type2 = currentPokemon.getType2();
        float baseY = 98f;
        float startX = 85f;
        float iconWidth = 32f;
        float iconHeight = 12f;
        float spacing = 4f;
        String typeLabel = "TYPE /";
        layout.setText(blackFont, typeLabel);
        float labelY = baseY + (iconHeight / 2) + (layout.height / 2);
        blackFont.draw(batch, layout, xOffset + startX, labelY);
        float currentX = startX + layout.width + spacing;
        if (type1 != null) {
            TextureRegion type1Region = typeAtlas.findRegion(type1.name().toLowerCase());
            if (type1Region != null) {
                batch.draw(type1Region, xOffset + currentX, baseY, iconWidth, iconHeight);
                currentX += iconWidth + spacing;
            }
        }
        if (type2 != null) {
            TextureRegion type2Region = typeAtlas.findRegion(type2.name().toLowerCase());
            if (type2Region != null) {
                batch.draw(type2Region, xOffset + currentX, baseY, iconWidth, iconHeight);
            }
        }
    }

    private void drawAbilityInfo(float xOffset) {
        Ability ability = currentPokemon.getAbility();
        if (ability == null) return;
        float startX = 85f;
        float nameY  = 85f;
        String abilityName = ability.getName() != null ? ability.getName().toUpperCase() : "";
        whiteFont.draw(batch, abilityName, xOffset + startX, nameY);
        String description = ability.getDescription() != null ? ability.getDescription() : "";
        float descY = nameY - 15f;
        layout.setText(blackFont, description, new Color(96f/255f,96f/255f,96f/255f,1f), 150f, Align.left, true);
        blackFont.draw(batch, layout, xOffset + startX, descY);
    }

    private void drawMetInformation(float xOffset) {
        float startX = 85f;
        float y = 45f;
        float currentX = startX;
        Color originalColor = whiteFont.getColor().cpy();
        String natureStr = currentPokemon.getNature().name().toUpperCase();
        whiteFont.setColor(new Color(248f/255f, 0f, 0f, 0.75f));
        layout.setText(whiteFont, natureStr);
        whiteFont.draw(batch, layout, xOffset + currentX, y);
        currentX += layout.width;
        String textPart1 = " nature, met at Lv";
        blackFont.draw(batch, textPart1, xOffset + currentX, y);
        layout.setText(blackFont, textPart1);
        currentX += layout.width;
        String metLevelStr = String.valueOf(currentPokemon.getMetLevel());
        whiteFont.setColor(new Color(248f/255f, 0f, 0f, 0.7f));
        layout.setText(whiteFont, metLevelStr);
        whiteFont.draw(batch, layout, xOffset + currentX, y);
        currentX += layout.width;
        String textPart2 = ",";
        blackFont.draw(batch, textPart2, xOffset + currentX, y);
        layout.setText(blackFont, textPart2);
        currentX += layout.width;
        currentX = startX;
        y -= 15;
        String mapNameStr = currentPokemon.getMetMap();
        whiteFont.setColor(new Color(248f/255f, 0f, 0f, 0.7f));
        layout.setText(whiteFont, mapNameStr);
        whiteFont.draw(batch, layout, xOffset + currentX, y);
        currentX += layout.width;
        String textPart3 = ".";
        blackFont.draw(batch, textPart3, xOffset + currentX, y);
        whiteFont.setColor(originalColor);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(Gdx.input.getInputProcessor());
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        if (pokemonAnimation != null) {
            for (Object frameObject : pokemonAnimation.getKeyFrames()) {
                TextureRegion frame = (TextureRegion) frameObject;
                if (frame.getTexture() != null) {
                    frame.getTexture().dispose();
                }
            }
        }
    }
}