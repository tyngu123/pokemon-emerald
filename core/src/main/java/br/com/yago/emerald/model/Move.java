package br.com.yago.emerald.model;

/**
 * Representa um movimento (ataque) que um Pokémon pode aprender e usar em batalha.
 */
public class Move {

    private final String name;
    private final String description;
    private final PokemonType type;
    private final MoveCategory category;
    private final int power;
    private final int accuracy;
    private final int maxPP;
    private final int priority; // NOVO CAMPO
    private int currentPP;

    public Move(String name, String description, PokemonType type, MoveCategory category, int power, int accuracy, int pp, int priority) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.category = category;
        this.power = power;
        this.accuracy = accuracy;
        this.maxPP = pp;
        this.currentPP = pp; // Começa com PP cheio
        this.priority = priority; // NOVO
    }

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public PokemonType getType() { return type; }
    public MoveCategory getCategory() { return category; }
    public int getPower() { return power; }
    public int getAccuracy() { return accuracy; }
    public int getMaxPP() { return maxPP; }
    public int getCurrentPP() { return currentPP; }
    public int getPriority() { return priority; } // NOVO GETTER

    /**
     * Retorna a representação do poder como String. Se for 0, mostra "--".
     */
    public String getPowerString() {
        return power == 0 ? "--" : String.valueOf(power);
    }

    /**
     * Retorna a representação da precisão como String. Se for 0 (nunca erra), mostra "--".
     */
    public String getAccuracyString() {
        return accuracy == 0 ? "--" : String.valueOf(accuracy);
    }
}