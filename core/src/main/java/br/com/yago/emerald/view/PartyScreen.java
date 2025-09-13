package br.com.yago.emerald.view;

import br.com.yago.emerald.EmeraldGame;
import br.com.yago.emerald.assets.AssetsLoader;
import br.com.yago.emerald.model.Party;
import br.com.yago.emerald.model.Pokemon;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class PartyScreen extends ScreenAdapter {

    private final EmeraldGame game;
    private final GameScreen gameScreen;
    private final SpriteBatch batch;
    private final AssetsLoader assetLoader;
    private final Viewport viewport;
    private final OrthographicCamera camera;
    private final ShapeRenderer shapeRenderer;

    private final BitmapFont whiteFont;
    private final BitmapFont blackFont;
    private final GlyphLayout layout = new GlyphLayout();

    private final Texture background;
    private final TextureAtlas iconAtlas;
    private final TextureAtlas partyUiAtlas;
    private final NinePatch menuBoxPatch;

    private final Party playerParty;
    private int selectedIndex = 0;

    private enum PartyState { BROWSE, ACTION_MENU, SWITCHING }
    private PartyState partyState = PartyState.BROWSE;

    private final Array<String> actionMenuOptions = new Array<>(new String[]{"SUMMARY", "SWITCH", "ITEM", "CANCEL"});
    private int selectedActionIndex = 0;

    private int switchInitialIndex = -1;

    private final Color hpBarColor = Color.valueOf("70F8A8");
    private float iconBounceTimer = 0f;
    private final float ICON_BOUNCE_SPEED = 8f;
    private final float ICON_BOUNCE_AMPLITUDE = 2f;

    private boolean isSwapAnimating = false;
    private int animFromIndex = -1, animToIndex = -1;
    private float animTimer = 0f;
    private static final float SWAP_OUT_DURATION = 0.22f;
    private static final float SWAP_IN_DURATION  = 0.22f;

    private enum SwapPhase { OUT, COMMIT, IN, DONE }
    private SwapPhase swapPhase = SwapPhase.DONE;
    
    private final BattleScreen battleScreen;
    private final boolean isMandatorySwitch;

    public PartyScreen(EmeraldGame game, GameScreen gameScreen, SpriteBatch batch, AssetsLoader assetLoader, BattleScreen battleScreen, boolean isMandatorySwitch) {
        this.game = game;
        this.gameScreen = gameScreen;
        this.batch = batch;
        this.assetLoader = assetLoader;
        this.camera = new OrthographicCamera();
        this.viewport = new ExtendViewport(240, 160, camera);
        this.shapeRenderer = new ShapeRenderer();
        this.battleScreen = battleScreen;
        this.isMandatorySwitch = isMandatorySwitch;

        this.whiteFont = assetLoader.manager.get(assetLoader.PARTY_FONT_NAME + ".otf", BitmapFont.class);
        this.blackFont = assetLoader.manager.get(assetLoader.BAG_FONT_NAME + ".otf", BitmapFont.class);

        this.iconAtlas = assetLoader.manager.get(assetLoader.POKEMON_ICON_ATLAS, TextureAtlas.class);
        this.partyUiAtlas = assetLoader.manager.get(assetLoader.PARTY_UI_ATLAS, TextureAtlas.class);
        this.menuBoxPatch = new NinePatch(assetLoader.manager.get(assetLoader.BOX, Texture.class), 8, 8, 8, 8);
        this.background = new Texture(Gdx.files.internal("ui/party_bg.png"));
        this.playerParty = gameScreen.getPlayer().getParty();
    }

    public PartyScreen(EmeraldGame game, GameScreen gameScreen, SpriteBatch batch, AssetsLoader assetLoader) {
        this(game, gameScreen, batch, assetLoader, null, false);
    }
    
    public PartyScreen(EmeraldGame game, GameScreen gameScreen, SpriteBatch batch, AssetsLoader assetLoader, BattleScreen battleScreen) {
        this(game, gameScreen, batch, assetLoader, battleScreen, false);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                switch (partyState) {
                    case BROWSE:
                        return handleBrowseInput(keycode);
                    case ACTION_MENU:
                        return handleActionMenuInput(keycode);
                    case SWITCHING:
                        return handleSwitchingInput(keycode);
                }
                return false;
            }
        });
    }

    private boolean handleBrowseInput(int keycode) {
        if (isSwapAnimating) return true;

        int partySize = playerParty.getPartySize();
        int totalOptions = partySize + 1;

        if (keycode == Input.Keys.DOWN) {
            selectedIndex = (selectedIndex + 1) % totalOptions;
            return true;
        }
        if (keycode == Input.Keys.UP) {
            selectedIndex = (selectedIndex - 1 + totalOptions) % totalOptions;
            return true;
        }
        if (keycode == Input.Keys.RIGHT) {
            if (selectedIndex == 0 && partySize > 1) selectedIndex = 1;
            return true;
        }
        if (keycode == Input.Keys.LEFT) {
            if (selectedIndex > 0 && selectedIndex < partySize) selectedIndex = 0;
            return true;
        }
        if (keycode == Input.Keys.X || keycode == Input.Keys.ESCAPE) {
            if (isMandatorySwitch) {
                return true; 
            }
            if (battleScreen != null) {
                game.setScreen(battleScreen);
                return true;
            }
            game.closePartyScreen();
            return true;
        }
        if (keycode == Input.Keys.Z) {
            if (selectedIndex == partySize) {
                if (isMandatorySwitch) {
                    return true;
                }
                if (battleScreen != null) {
                    game.setScreen(battleScreen);
                } else {
                    game.closePartyScreen();
                }
            } else {
                Pokemon selectedMon = playerParty.getPokemon(selectedIndex);
                if (isMandatorySwitch && selectedMon.getCurrentHP() <= 0) {
                    return true;
                }
                partyState = PartyState.ACTION_MENU;
                selectedActionIndex = 0;
            }
            return true;
        }
        return false;
    }

    private boolean handleActionMenuInput(int keycode) {
        if (isSwapAnimating) return true;

        if (keycode == Input.Keys.DOWN) {
            selectedActionIndex = (selectedActionIndex + 1) % actionMenuOptions.size;
            return true;
        }
        if (keycode == Input.Keys.UP) {
            selectedActionIndex = (selectedActionIndex - 1 + actionMenuOptions.size) % actionMenuOptions.size;
            return true;
        }
        if (keycode == Input.Keys.X || keycode == Input.Keys.ESCAPE) {
            partyState = PartyState.BROWSE;
            return true;
        }
        if (keycode == Input.Keys.Z) {
            String selectedOption = actionMenuOptions.get(selectedActionIndex);
            Pokemon selectedMon = playerParty.getPokemon(selectedIndex);
            
            switch (selectedOption) {
                case "SUMMARY":
                    game.openSummaryScreen(playerParty, selectedIndex);
                    break;
                case "SWITCH":
                    if (battleScreen != null) {
                        if (selectedMon.getCurrentHP() <= 0 || selectedMon == battleScreen.getPlayerPokemon()) {
                            return true;
                        }
                        battleScreen.playerSendsNewPokemon(selectedMon, !isMandatorySwitch);
                        game.setScreen(battleScreen);
                    } else {
                        switchInitialIndex = selectedIndex;
                        partyState = PartyState.SWITCHING;
                    }
                    break;
                case "CANCEL":
                    partyState = PartyState.BROWSE;
                    break;
                default:
                    Gdx.app.log("PartyScreen", "Ação '" + selectedOption + "' não implementada.");
                    partyState = PartyState.BROWSE;
                    break;
            }
            return true;
        }
        return false;
    }

    private boolean handleSwitchingInput(int keycode) {
        if (isSwapAnimating) return true;

        int partySize = playerParty.getPartySize();

        if (keycode == Input.Keys.DOWN) {
            selectedIndex = (selectedIndex + 1) % partySize;
            return true;
        }
        if (keycode == Input.Keys.UP) {
            selectedIndex = (selectedIndex - 1 + partySize) % partySize;
            return true;
        }
        if (keycode == Input.Keys.RIGHT) {
            if (selectedIndex == 0 && partySize > 1) selectedIndex = 1;
            return true;
        }
        if (keycode == Input.Keys.LEFT) {
            if (selectedIndex > 0) selectedIndex = 0;
            return true;
        }
        if (keycode == Input.Keys.X || keycode == Input.Keys.ESCAPE) {
            partyState = PartyState.ACTION_MENU;
            selectedIndex = switchInitialIndex;
            switchInitialIndex = -1;
            return true;
        }
        if (keycode == Input.Keys.Z) {
            beginSwapAnimation(switchInitialIndex, selectedIndex);
            return true;
        }
        return false;
    }

    private void beginSwapAnimation(int from, int to) {
        if (from == to) {
            partyState = PartyState.BROWSE;
            switchInitialIndex = -1;
            return;
        }
        isSwapAnimating = true;
        animFromIndex = from;
        animToIndex   = to;
        animTimer     = 0f;
        swapPhase     = SwapPhase.OUT;
    }

    private float getSlideOffsetX(int index) {
        if (!isSwapAnimating) return 0f;

        float dist = viewport.getWorldWidth() + 32f;

        switch (swapPhase) {
            case OUT: {
                if (index == animFromIndex || index == animToIndex) {
                    float p = Math.min(1f, animTimer / SWAP_OUT_DURATION);
                    float k = Interpolation.pow2Out.apply(p);
                    return -dist * k;
                }
                return 0f;
            }
            case IN: {
                if (index == animFromIndex || index == animToIndex) {
                    float p = Math.min(1f, animTimer / SWAP_IN_DURATION);
                    float k = Interpolation.pow2Out.apply(p);
                    return dist * (1f - k);
                }
                return 0f;
            }
            default:
                return 0f;
        }
    }

    private void update(float delta) {
        iconBounceTimer += delta;

        if (isSwapAnimating) {
            animTimer += delta;

            if (swapPhase == SwapPhase.OUT && animTimer >= SWAP_OUT_DURATION) {
                playerParty.swapPokemons(animFromIndex, animToIndex);
                swapPhase = SwapPhase.IN;
                animTimer = 0f;
            } else if (swapPhase == SwapPhase.IN && animTimer >= SWAP_IN_DURATION) {
                isSwapAnimating = false;
                swapPhase = SwapPhase.DONE;
                partyState = PartyState.BROWSE;
                selectedIndex = animToIndex;
                switchInitialIndex = -1;
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
        batch.draw(background, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        for (int i = 0; i < playerParty.getPartySize(); i++) {
            drawSlotAndIcon(playerParty.getPokemon(i), i);
        }
        for (int i = 0; i < playerParty.getPartySize(); i++) {
            drawHpBarForPokemon(playerParty.getPokemon(i), i);
        }
        for (int i = 0; i < playerParty.getPartySize(); i++) {
            drawTextForPokemon(playerParty.getPokemon(i), i);
        }

        drawCancelButton();
        drawInfoBox();
        if (partyState == PartyState.ACTION_MENU) {
            drawActionMenu();
        }

        batch.end();
    }

    private void drawSlotAndIcon(Pokemon pokemon, int index) {
        boolean isMainSlot = (index == 0);
        float slotX, slotY;
        TextureRegion slotSprite;

        if (isMainSlot) {
            slotX = 6f;
            slotY = 88f;
            if (partyState == PartyState.SWITCHING) {
                if (index == switchInitialIndex) {
                    slotSprite = partyUiAtlas.findRegion("party_slot_switch");
                } else if (index == selectedIndex) {
                    slotSprite = partyUiAtlas.findRegion("party_slot_switch_selected");
                } else {
                    slotSprite = partyUiAtlas.findRegion("party_slot");
                }
            } else {
                slotSprite = (selectedIndex == index && partyState == PartyState.BROWSE) ?
                        partyUiAtlas.findRegion("party_slot_selected") :
                        partyUiAtlas.findRegion("party_slot");
            }
        } else {
            slotX = 96f;
            slotY = 30f + (4 - (index - 1)) * 26f;
            if (partyState == PartyState.SWITCHING) {
                if (index == switchInitialIndex) {
                    slotSprite = partyUiAtlas.findRegion("party_slot_switch_other");
                } else if (index == selectedIndex) {
                    slotSprite = partyUiAtlas.findRegion("party_slot_switch_other_selected");
                } else {
                    slotSprite = partyUiAtlas.findRegion("party_slot_other");
                }
            } else {
                slotSprite = (selectedIndex == index && partyState == PartyState.BROWSE) ?
                        partyUiAtlas.findRegion("party_slot_other_selected") :
                        partyUiAtlas.findRegion("party_slot_other");
            }
        }

        float slideX = getSlideOffsetX(index);

        batch.draw(slotSprite, slotX + slideX, slotY);

        TextureRegion pokeballSprite = (selectedIndex == index && partyState == PartyState.BROWSE) ?
                partyUiAtlas.findRegion("party_pokeball_selected") :
                partyUiAtlas.findRegion("party_pokeball");

        float iconX = (slotX + slideX) + (isMainSlot ? -26 : -27);
        float baseIconY = slotY + (isMainSlot ? 26 : -6);

        if (pokeballSprite != null) {
            float pokeballX = (slotX + slideX) + (12f / 2f) - (pokeballSprite.getRegionWidth() / 2f);
            float pokeballY = baseIconY + (64f / 4f) - (pokeballSprite.getRegionHeight() / 2f);
            batch.draw(pokeballSprite, pokeballX, pokeballY);
        }

        TextureRegion icon = iconAtlas.findRegion(pokemon.getIconSpriteName());
        if (icon != null) {
            float yOffset = 0;
            if (selectedIndex == index && (partyState == PartyState.BROWSE || partyState == PartyState.SWITCHING)) {
                if ((int) (iconBounceTimer * ICON_BOUNCE_SPEED) % 2 == 0) yOffset = ICON_BOUNCE_AMPLITUDE;
            }
            batch.draw(icon, iconX, baseIconY + yOffset);
        }
    }

    private void drawHpBarForPokemon(Pokemon pokemon, int index) {
        boolean isMainSlot = (index == 0);
        float slotX = isMainSlot ? 6f : 96f;
        float slotY = isMainSlot ? 88f : 30f + (4 - (index - 1)) * 26f;
        float hpBarX = slotX + (isMainSlot ? 24 : 88);
        float hpBarY = slotY + (isMainSlot ? 13 : 11);

        float slideX = getSlideOffsetX(index);
        drawHpBar(pokemon, hpBarX + slideX, hpBarY);
    }

    private void drawTextForPokemon(Pokemon pokemon, int index) {
        boolean isMainSlot = (index == 0);
        float slotX = isMainSlot ? 6f : 96f;
        float slotY = isMainSlot ? 88f : 30f + (4 - (index - 1)) * 26f;
        float textX = slotX + (isMainSlot ? 20 : 16);
        float nameY = slotY + (isMainSlot ? 42 : 26);
        float levelY = nameY - 12;
        float levelX = slotX + (isMainSlot ? 25 : 18);

        float slideX = getSlideOffsetX(index);

        whiteFont.draw(batch, pokemon.getNickname(), textX + slideX, nameY);

        String levelStr = "Lv" + pokemon.getLevel();
        layout.setText(whiteFont, levelStr);
        whiteFont.draw(batch, levelStr, levelX + slideX, levelY);

        TextureRegion genderIcon = null;
        try {
            Pokemon.Gender g = pokemon.getGender();
            if (g != null) {
                if (g.name().equalsIgnoreCase("MALE")) {
                    genderIcon = partyUiAtlas.findRegion("party_male");
                } else if (g.name().equalsIgnoreCase("FEMALE")) {
                    genderIcon = partyUiAtlas.findRegion("party_female");
                }
            }
        } catch (Exception ignored) {}

        if (genderIcon != null) {
            float pad = 3f;
            float iconW = genderIcon.getRegionWidth();
            float iconH = genderIcon.getRegionHeight();
            float genderX = levelX + slideX + layout.width + pad;
            float genderY = levelY - layout.height + (layout.height - iconH) / 2f;
            batch.draw(genderIcon, genderX, genderY, iconW, iconH);
        }

        String hpString = pokemon.getCurrentHP() + "/" + pokemon.getMaxHP();
        layout.setText(whiteFont, hpString);
        float hpTextY, hpTextX;
        if (isMainSlot) {
            hpTextY = slotY + 10;
            hpTextX = slotX + 40;
        } else {
            hpTextY = slotY + 6;
            hpTextX = (slotX + 88 + 48) - layout.width;
        }
        whiteFont.draw(batch, hpString, hpTextX + slideX, hpTextY);
    }

    private void drawCancelButton() {
        float buttonX = 180f;
        float buttonY = 20f;
        String text = "CANCEL";
        layout.setText(whiteFont, text);
        if (selectedIndex == playerParty.getPartySize() && partyState == PartyState.BROWSE) {
            batch.draw(assetLoader.manager.get(assetLoader.HORIZONTAL_ARROW, Texture.class), buttonX - 10, buttonY - (layout.height / 2), 7, 7);
        }
        whiteFont.draw(batch, layout, buttonX + 14, buttonY + 2);
    }

    private void drawHpBar(Pokemon pokemon, float x, float y) {
        batch.end();
        float barWidth = 48;
        float barHeight = 3;
        float hpPercentage = (float) pokemon.getCurrentHP() / pokemon.getMaxHP();
        Color hpColor = hpBarColor;
        if (hpPercentage < 0.5f) hpColor = Color.YELLOW;
        if (hpPercentage < 0.2f) hpColor = Color.RED;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(x, y, barWidth, barHeight);
        shapeRenderer.setColor(hpColor);
        shapeRenderer.rect(x, y, barWidth * hpPercentage, barHeight);
        shapeRenderer.end();
        batch.begin();
    }

    private void drawInfoBox() {
        String text;
        float boxWidth;
        float boxX = 4f;
        float boxY = 0f;
        float boxHeight = 28f;

        if(isMandatorySwitch) {
            text = "Choose a POKEMON.";
            boxWidth = 160f;
        } else if (partyState == PartyState.BROWSE) {
            text = "Choose a POKEMON.";
            boxWidth = 160f;
        } else if (partyState == PartyState.ACTION_MENU) {
            text = "Do what with this PKMN?";
            boxWidth = 140f;
        } else { // SWITCHING
            text = "Move to where?";
            boxWidth = 140f;
        }
        menuBoxPatch.draw(batch, boxX, boxY, boxWidth, boxHeight);
        layout.setText(blackFont, text, new Color(0.282f, 0.282f, 0.282f, 0.7f), boxWidth - 10, Align.left, true);
        blackFont.draw(batch, layout, boxX + 8, boxY + boxHeight - 8);
    }

    private void drawActionMenu() {
        float boxWidth = 90f;
        float boxHeight = 80f;
        float boxX = 150f;
        float boxY = 0f;
        Color originalColor = blackFont.getColor().cpy();

        menuBoxPatch.draw(batch, boxX, boxY, boxWidth, boxHeight);

        float verticalPadding = 10f;
        float itemSpacing = 16f;

        for (int i = 0; i < actionMenuOptions.size; i++) {
            String optionText = actionMenuOptions.get(i);
            boolean enabled = true;
            
            if (battleScreen != null) {
                Pokemon selectedMon = playerParty.getPokemon(selectedIndex);
                if (optionText.equals("SWITCH") && (selectedMon.getCurrentHP() <= 0 || selectedMon == battleScreen.getPlayerPokemon())) {
                    enabled = false;
                }
            }
            
            if (isMandatorySwitch) {
                if (!optionText.equals("SUMMARY") && !optionText.equals("SWITCH")) {
                    enabled = false;
                }
                 if (optionText.equals("CANCEL")) { // A opção CANCEL não deve ser utilizável em troca forçada
                    enabled = false;
                }
            }

            if (!enabled) {
                blackFont.setColor(Color.GRAY);
            }

            blackFont.draw(batch, optionText, boxX + 15f, boxY + boxHeight - verticalPadding - (i * itemSpacing));

            if (!enabled) {
                blackFont.setColor(originalColor);
            }
        }

        layout.setText(blackFont, actionMenuOptions.get(selectedActionIndex));
        float arrowY = boxY + boxHeight - verticalPadding - (selectedActionIndex * itemSpacing) - (layout.height / 2f) - (7f / 2f);
        batch.draw(assetLoader.manager.get(assetLoader.HORIZONTAL_ARROW, Texture.class), boxX + 5f, arrowY, 7f, 7f);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void hide() {
        if(battleScreen == null) {
            Gdx.input.setInputProcessor(gameScreen.getInputHandler());
        }
    }

    @Override
    public void dispose() {
        background.dispose();
        shapeRenderer.dispose();
    }
}