package br.com.yago.emerald.database;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
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
        
        Array<PokemonData> pokemonList = json.fromJson(Array.class, PokemonData.class, Gdx.files.internal("data/pokemon_db.json"));
        for (PokemonData data : pokemonList) {
            pokemonDataMap.put(data.speciesName.toUpperCase(), data);
        }
        
        JsonValue moveRoot = new JsonReader().parse(Gdx.files.internal("data/moves_db.json"));
        for (JsonValue moveEntry = moveRoot.child; moveEntry != null; moveEntry = moveEntry.next) {
            MoveData moveData = json.readValue(MoveData.class, moveEntry);
            moveDataMap.put(moveEntry.name().toUpperCase(), moveData);
        }

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
        
        PokemonNature randomNature = PokemonNature.values()[MathUtils.random(PokemonNature.values().length - 1)];
        Pokemon.Gender randomGender = MathUtils.randomBoolean() ? Pokemon.Gender.MALE : Pokemon.Gender.FEMALE;
        boolean isShiny = MathUtils.random(1, 4096) == 1;

        Pokemon pokemon = new Pokemon(
            data,
            level,
            (nickname == null || nickname.isEmpty()) ? speciesName.substring(0, 1).toUpperCase() + speciesName.substring(1).toLowerCase() : nickname,
            randomGender,
            isShiny,
            randomNature,
            level,
            "Wild",
            Pokeball.POKEBALL,
            trainerName,
            ability
        );
        
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

    // Novo método para criar Pokémon com XP específico
    public Pokemon createPokemon(String speciesName, String nickname, String trainerName, int experiencePoints) {
        PokemonData data = getPokemonData(speciesName);
        if (data == null) {
            Gdx.app.error("GameDatabase", "Espécie não encontrada no banco de dados: " + speciesName);
            return null;
        }
        
        Ability ability = getAbility(data.ability);
        
        PokemonNature randomNature = PokemonNature.values()[MathUtils.random(PokemonNature.values().length - 1)];
        Pokemon.Gender randomGender = MathUtils.randomBoolean() ? Pokemon.Gender.MALE : Pokemon.Gender.FEMALE;
        boolean isShiny = MathUtils.random(1, 4096) == 1;

        Pokemon pokemon = new Pokemon(
            data,
            (nickname == null || nickname.isEmpty()) ? speciesName.substring(0, 1).toUpperCase() + speciesName.substring(1).toLowerCase() : nickname,
            randomGender,
            isShiny,
            randomNature,
            "Wild",
            Pokeball.POKEBALL,
            trainerName,
            ability,
            experiencePoints
        );
        
        if (data.learnset != null) {
            for (LearnableMove learnable : data.learnset) {
                if (learnable.level <= pokemon.getLevel()) {
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