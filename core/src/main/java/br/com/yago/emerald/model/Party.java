package br.com.yago.emerald.model;

import com.badlogic.gdx.utils.Array;

public class Party {
    
    public static final int MAX_PARTY_SIZE = 6;
    
    private final Array<Pokemon> pokemons = new Array<>(MAX_PARTY_SIZE);
    
    public boolean addPokemon(Pokemon pokemon) {
        if (pokemons.size < MAX_PARTY_SIZE) {
            pokemons.add(pokemon);
            return true;
        }
        return false; // Party está cheia
    }
    
    /**
     * ## NOVO MÉTODO ##
     * Troca a posição de dois Pokémon na party.
     * @param index1 Índice do primeiro Pokémon.
     * @param index2 Índice do segundo Pokémon.
     */
    public void swapPokemons(int index1, int index2) {
        if (index1 >= 0 && index1 < pokemons.size &&
            index2 >= 0 && index2 < pokemons.size) {
            pokemons.swap(index1, index2);
        }
    }
    
    public Pokemon getPokemon(int index) {
        if (index >= 0 && index < pokemons.size) {
            return pokemons.get(index);
        }
        return null;
    }
    
    public Array<Pokemon> getPokemons() {
        return pokemons;
    }
    
    public int getPartySize() {
        return pokemons.size;
    }
    
    /**
     * ## NOVO MÉTODO ##
     * Retorna o primeiro Pokémon na party que não está desmaiado.
     * @return O primeiro Pokémon saudável, ou null se todos estiverem desmaiados.
     */
    public Pokemon getFirstAvailablePokemon() {
        for (Pokemon p : pokemons) {
            if (p.getCurrentHP() > 0) {
                return p;
            }
        }
        return null;
    }
}