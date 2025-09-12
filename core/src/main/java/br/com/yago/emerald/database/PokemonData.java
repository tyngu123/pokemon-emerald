package br.com.yago.emerald.database;

import com.badlogic.gdx.utils.Array;
import java.util.HashMap;

public class PokemonData {
    public String speciesName;
    public HashMap<String, Integer> baseStats;
    public String type1;
    public String type2;
    public String ability;
    public String experienceGroup;
    public Array<LearnableMove> learnset;
    public int baseExperienceYield; // NOVO CAMPO

    public PokemonData() {}
}