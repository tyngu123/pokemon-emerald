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
import com.badlogic.gdx.utils.Align; // <--- NOVO (para wrap da descrição)
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import br.com.yago.emerald.EmeraldGame;
import br.com.yago.emerald.assets.AnimationLoader;
import br.com.yago.emerald.assets.AssetsLoader;
import br.com.yago.emerald.model.Ability; // <--- NOVO
import br.com.yago.emerald.model.Party;
import br.com.yago.emerald.model.Pokeball;
import br.com.yago.emerald.model.Pokemon;
import br.com.yago.emerald.model.Pokemon.Gender;
import br.com.yago.emerald.model.PokemonType;

public class SummaryScreen extends ScreenAdapter {

    private final EmeraldGame game;
    private final SpriteBatch batch;
    private final Viewport viewport;
    private final OrthographicCamera camera;
    private final Texture background;
    
    private final BitmapFont whiteFont;
    private final BitmapFont blackFont;
    private final GlyphLayout layout = new GlyphLayout();

    private final TextureAtlas typeAtlas;
    private final TextureAtlas pokeballsAtlas;
    private final TextureAtlas partyUiAtlas;
    
    private final Party party;
    private int currentIndex;
    private Pokemon currentPokemon;
    private Animation<TextureRegion> pokemonAnimation;
    private float stateTimer = 0f;

    public SummaryScreen(EmeraldGame game, SpriteBatch batch, AssetsLoader assetLoader, Party party, int initialIndex) {
        this.game = game;
        this.batch = batch;
        this.party = party;
        this.camera = new OrthographicCamera();
        this.viewport = new ExtendViewport(240, 160, camera);
        
        this.background = assetLoader.manager.get(assetLoader.SUMMARY_UI, Texture.class);
        this.whiteFont = assetLoader.manager.get(assetLoader.PARTY_FONT_NAME + ".otf", BitmapFont.class);
        this.blackFont = assetLoader.manager.get(assetLoader.BAG_FONT_NAME + ".otf", BitmapFont.class);
        
        this.typeAtlas = assetLoader.manager.get(assetLoader.TYPE_ATLAS, TextureAtlas.class);
        this.pokeballsAtlas = assetLoader.manager.get(assetLoader.POKEBALLS_ATLAS, TextureAtlas.class);
        this.partyUiAtlas = assetLoader.manager.get(assetLoader.PARTY_UI_ATLAS, TextureAtlas.class);

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
    }
    
    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.X || keycode == Input.Keys.ESCAPE) {
                    game.closeSummaryScreen();
                    return true;
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
                return false;
            }
        });
    }
    
    @Override
    public void render(float delta) {
        stateTimer += delta;

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(background, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        if (pokemonAnimation != null) {
            TextureRegion currentFrame = pokemonAnimation.getKeyFrame(stateTimer, true);
            batch.draw(currentFrame, 10, 64, 64, 64); // (inalterado)
        }

        drawPokeballIcon(); // (inalterado)

        String displayName = currentPokemon.getNickname() +  "\n" + " / " + currentPokemon.getSpeciesName();
        whiteFont.draw(batch, displayName, 4, 65); // (inalterado)
        
        drawLevelAndGender();      // (inalterado)
        drawOriginalTrainerInfo(); // (inalterado)
        drawPokemonTypes();        // (inalterado)

        // ===== NOVO BLOCO: Ability =====
        drawAbilityInfo();

        drawMetInformation();      // (inalterado)

        batch.end();
    }

    private void drawPokeballIcon() {
        Pokeball pokeball = currentPokemon.getPokeball();
        if (pokeball != null) {
            TextureRegion pokeballRegion = pokeballsAtlas.findRegion(pokeball.name().toLowerCase());
            if (pokeballRegion != null) {
                batch.draw(pokeballRegion, 4, 18, 12, 12); // (inalterado)
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

    /**
     * (inalterado) OT / <nome>
     */
    private void drawOriginalTrainerInfo() {
        float startX = 85f;
        float y = 125f; // (inalterado)
        float currentX = startX;

        String otLabel = "OT /";
        layout.setText(blackFont, otLabel);
        blackFont.draw(batch, layout, currentX, y);
        currentX += layout.width + 4;

        String trainerName = currentPokemon.getOriginalTrainerName();
        whiteFont.draw(batch, trainerName, currentX, y);
    }

    private void drawPokemonTypes() {
        PokemonType type1 = currentPokemon.getType1();
        PokemonType type2 = currentPokemon.getType2();

        float baseY = 98f;      // (inalterado)
        float startX = 85f;     // (inalterado)
        float iconWidth = 32f;  // (inalterado)
        float iconHeight = 12f; // (inalterado)
        float spacing = 4f;     // (inalterado)

        String typeLabel = "TYPE /";
        layout.setText(blackFont, typeLabel);
        
        float labelY = baseY + (iconHeight / 2) + (layout.height / 2);
        blackFont.draw(batch, layout, startX, labelY);

        float currentX = startX + layout.width + spacing;
        
        if (type1 != null) {
            TextureRegion type1Region = typeAtlas.findRegion(type1.name().toLowerCase());
            if (type1Region != null) {
                batch.draw(type1Region, currentX, baseY, iconWidth, iconHeight);
                currentX += iconWidth + spacing;
            }
        }

        if (type2 != null) {
            TextureRegion type2Region = typeAtlas.findRegion(type2.name().toLowerCase());
            if (type2Region != null) {
                batch.draw(type2Region, currentX, baseY, iconWidth, iconHeight);
            }
        }
    }

    /**
     * ===== NOVO MÉTODO =====
     * Exibe Ability (nome + descrição) entre os tipos (y≈98) e o bloco "met info" (y≈45),
     * sem deslocar nada do layout original.
     */
    private void drawAbilityInfo() {
        Ability ability = currentPokemon.getAbility();
        if (ability == null) return;

        float startX = 85f;
        float nameY  = 85f; // abaixo dos tipos (98) e acima do met info (45)
        String abilityName = ability.getName() != null ? ability.getName().toUpperCase() : "";

        // Nome da habilidade em branco
        whiteFont.draw(batch, abilityName, startX, nameY);

        // Descrição em preto, com wrap em ~150px para caber na coluna direita
        String description = ability.getDescription() != null ? ability.getDescription() : "";
        float descY = nameY - 15f; // 15px abaixo do nome
        layout.setText(blackFont, description, new Color(96f,96f,96f,1f), 150f, Align.left, true);
        blackFont.draw(batch, layout, startX, descY);
        // Observação: se a descrição for grande demais, considere truncar ou paginar em outra tela.
    }
    
    private void drawMetInformation() {
        float startX = 85f;
        float y = 45f; // (inalterado)
        float currentX = startX;

        Color originalColor = whiteFont.getColor().cpy();

        String natureStr = currentPokemon.getNature().name().toUpperCase();
        whiteFont.setColor(new Color(248f/255f, 0f, 0f, 0.75f));
        layout.setText(whiteFont, natureStr);
        whiteFont.draw(batch, layout, currentX, y);
        currentX += layout.width;
        
        String textPart1 = " nature, met at Lv";
        blackFont.draw(batch, textPart1, currentX, y);
        layout.setText(blackFont, textPart1);
        currentX += layout.width;

        String metLevelStr = String.valueOf(currentPokemon.getMetLevel());
        whiteFont.setColor(new Color(248f/255f, 0f, 0f, 0.7f));
        layout.setText(whiteFont, metLevelStr);
        whiteFont.draw(batch, layout, currentX, y);
        currentX += layout.width;

        String textPart2 = ",";
        blackFont.draw(batch, textPart2, currentX, y);
        layout.setText(blackFont, textPart2);
        currentX += layout.width;

        currentX = startX;
        y -= 15;

        String mapNameStr = currentPokemon.getMetMap();
        whiteFont.setColor(new Color(248f/255f, 0f, 0f, 0.7f));
        layout.setText(whiteFont, mapNameStr);
        whiteFont.draw(batch, layout, currentX, y);
        currentX += layout.width;
        
        String textPart3 = ".";
        blackFont.draw(batch, textPart3, currentX, y);

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
