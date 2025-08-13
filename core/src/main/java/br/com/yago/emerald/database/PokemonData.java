package br.com.yago.emerald.database;

import com.badlogic.gdx.utils.Array;
import java.util.HashMap; // Importa a classe concreta

// Mapeia uma entrada no pokemon_db.json
public class PokemonData {
    public String speciesName;
    // AQUI ESTÁ A CORREÇÃO: Troca de Map para HashMap
    public HashMap<String, Integer> baseStats; 
    public String type1;
    public String type2;
    public String ability;
    public String experienceGroup;
    public Array<LearnableMove> learnset;
    
    // Construtor vazio é necessário para a deserialização do JSON
    public PokemonData() {} 
}