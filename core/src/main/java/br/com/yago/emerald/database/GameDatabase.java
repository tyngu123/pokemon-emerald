package br.com.yago.emerald.database;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils; // Importação necessária para aleatoriedade
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Array;
import java.util.HashMap;
import java.util.Map;

import br.com.yago.emerald.model.*;

public class GameDatabase {

    private final Map<String, PokemonData> pokemonDataMap = new HashMap<>();
    private final Map<String, MoveData> moveDataMap = new HashMap<>();
    private final Map<String, Ability> abilityMap = new HashMap<>();

    public void load() {
        Json json = new Json();
        
        // Carrega Pokémon
        Array<PokemonData> pokemonList = json.fromJson(Array.class, PokemonData.class, Gdx.files.internal("data/pokemon_db.json"));
        for (PokemonData data : pokemonList) {
            pokemonDataMap.put(data.speciesName.toUpperCase(), data);
        }
        
        // Carrega Golpes
        JsonValue moveRoot = new JsonReader().parse(Gdx.files.internal("data/moves_db.json"));
        for (JsonValue moveEntry = moveRoot.child; moveEntry != null; moveEntry = moveEntry.next) {
            MoveData moveData = json.readValue(MoveData.class, moveEntry);
            moveDataMap.put(moveEntry.name().toUpperCase(), moveData);
        }

        // Carrega Habilidades
        JsonValue abilityRoot = new JsonReader().parse(Gdx.files.internal("data/abilities_db.json"));
        for (JsonValue abilityEntry = abilityRoot.child; abilityEntry != null; abilityEntry = abilityEntry.next) {
            AbilityData abilityData = json.readValue(AbilityData.class, abilityEntry);
            String abilityName = abilityEntry.name();
            abilityMap.put(abilityName.toUpperCase(), new Ability(abilityName, abilityData.description));
        }

        Gdx.app.log("GameDatabase", "Banco de dados carregado. " + pokemonDataMap.size() + " Pokémon, " + moveDataMap.size() + " golpes e " + abilityMap.size() + " habilidades.");
    }

    public PokemonData getPokemonData(String speciesName) {
        return pokemonDataMap.get(speciesName.toUpperCase());
    }

    public Move createMove(String moveName) {
        MoveData data = moveDataMap.get(moveName.toUpperCase());
        if (data == null) {
            Gdx.app.error("GameDatabase", "Golpe não encontrado no banco de dados: " + moveName);
            return null;
        }

        return new Move(
            moveName,
            data.description,
            PokemonType.valueOf(data.type),
            MoveCategory.valueOf(data.category),
            data.power,
            data.accuracy,
            data.pp
        );
    }
    
    public Ability getAbility(String abilityName) {
        Ability ability = abilityMap.get(abilityName.toUpperCase());
        if (ability == null) {
            Gdx.app.error("GameDatabase", "Habilidade não encontrada: " + abilityName);
            return new Ability("Unknown", "No description available.");
        }
        return ability;
    }

    public Pokemon createPokemon(String speciesName, int level, String nickname, String trainerName) {
        PokemonData data = getPokemonData(speciesName);
        if (data == null) {
            Gdx.app.error("GameDatabase", "Espécie não encontrada no banco de dados: " + speciesName);
            return null;
        }
        
        Ability ability = getAbility(data.ability);
        
        // ## AQUI ESTÁ A NOVA LÓGICA DE ALEATORIEDADE ##
        
        // 1. Nature Aleatória (já implementado)
        PokemonNature randomNature = PokemonNature.values()[MathUtils.random(PokemonNature.values().length - 1)];
        
        // 2. Gênero Aleatório (Macho ou Fêmea)
        Pokemon.Gender randomGender = MathUtils.randomBoolean() ? Pokemon.Gender.MALE : Pokemon.Gender.FEMALE;

        // 3. Chance de ser Shiny (1 em 4096)
        boolean isShiny = MathUtils.random(1, 4096) == 1;

        Pokemon pokemon = new Pokemon(
            data,
            level,
            (nickname == null || nickname.isEmpty()) ? speciesName.substring(0, 1).toUpperCase() + speciesName.substring(1).toLowerCase() : nickname,
            randomGender, // Usa o gênero aleatório
            isShiny,      // Usa o status shiny aleatório
            randomNature, // Usa a nature aleatória
            level,
            "Wild",       // metMap
            Pokeball.POKEBALL,
            trainerName,
            ability
        );
        
        // Adiciona os golpes corretos para o nível
        if (data.learnset != null) {
            for (LearnableMove learnable : data.learnset) {
                if (learnable.level <= level) {
                    Move move = createMove(learnable.moveName);
                    if (move != null) {
                        pokemon.learnMove(move);
                    }
                }
            }
        }
        
        return pokemon;
    }
}