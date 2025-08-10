package br.com.yago.emerald.model;

import java.util.Map;
import com.badlogic.gdx.utils.Array;

public class Pokemon {

    public enum Gender { MALE, FEMALE, GENDERLESS }

    private final String speciesName;
    private final String nickname;
    private final Gender gender;
    private final boolean shiny;
    private final int level;
    private int currentHP;
    private final int maxHP;
    private final int attack;
    private final int defense;
    private final int specialAttack;
    private final int specialDefense;
    private final int speed;

    private final PokemonType type1;
    private final PokemonType type2;

    private final PokemonNature nature;
    private final int metLevel;
    private final String metMap;

    private final Pokeball pokeball;
    private final String originalTrainerName;
    private final Ability ability;
    private final Item heldItem;

    private final ExperienceGroup experienceGroup;
    private final int experiencePoints;
    private final int experienceToNextLevel;

    // ## NOVO CAMPO PARA OS MOVIMENTOS ##
    private final Array<Move> moves;

    public Pokemon(
        String speciesName, String nickname, Pokemon.Gender gender, boolean shiny, int level,
        Map<String, Integer> baseStats,
        PokemonType type1, PokemonType type2, PokemonNature nature, int metLevel, String metMap,
        Pokeball pokeball, String originalTrainerName, Ability ability, Item heldItem,
        ExperienceGroup experienceGroup
    ) {
        this.speciesName = speciesName;
        this.nickname = nickname;
        this.gender = gender;
        this.shiny = shiny;
        this.level = level;
        this.nature = nature;
        this.metLevel = metLevel;
        this.metMap = metMap;
        this.pokeball = pokeball;
        this.originalTrainerName = originalTrainerName;
        this.ability = ability;
        this.heldItem = heldItem;
        this.experienceGroup = experienceGroup;
        this.type1 = type1;
        this.type2 = type2;
        this.moves = new Array<>(4); // Pokémon pode ter até 4 movimentos

        // Calcular stats usando a classe utilitária
        this.maxHP = StatCalculator.calculateHP(baseStats.get("HP"), level);
        this.attack = StatCalculator.calculateStat(baseStats.get("ATTACK"), level);
        this.defense = StatCalculator.calculateStat(baseStats.get("DEFENSE"), level);
        this.specialAttack = StatCalculator.calculateStat(baseStats.get("SPECIAL_ATTACK"), level);
        this.specialDefense = StatCalculator.calculateStat(baseStats.get("SPECIAL_DEFENSE"), level);
        this.speed = StatCalculator.calculateStat(baseStats.get("SPEED"), level);

        // O HP atual começa cheio
        this.currentHP = this.maxHP;

        // Calcular experiência usando a classe utilitária
        this.experiencePoints = ExperienceCalculator.getTotalExpForLevel(level, experienceGroup);
        this.experienceToNextLevel = ExperienceCalculator.getExpToNextLevel(level, experienceGroup);
    }

    /**
     * ## NOVO MÉTODO ##
     * Adiciona um movimento ao Pokémon, se houver espaço.
     * @param move O movimento a ser adicionado.
     */
    public void learnMove(Move move) {
        if (moves.size < 4) {
            moves.add(move);
        }
    }

    private int calculateStatWithNature(int baseStat, String statName) {
        float modifier = nature.getModifierForStat(statName);
        return (int) (baseStat * modifier);
    }

    // Getters
    public String getSpeciesName() { return speciesName; }
    public String getNickname() { return nickname; }
    public Gender getGender() { return gender; }
    public boolean isShiny() { return shiny; }
    public int getLevel() { return level; }
    public int getCurrentHP() { return currentHP; }
    public int getMaxHP() { return maxHP; }
    public int getAttack() { return calculateStatWithNature(this.attack, "ATTACK"); }
    public int getDefense() { return calculateStatWithNature(this.defense, "DEFENSE"); }
    public int getSpecialAttack() { return calculateStatWithNature(this.specialAttack, "SPECIAL_ATTACK"); }
    public int getSpecialDefense() { return calculateStatWithNature(this.specialDefense, "SPECIAL_DEFENSE"); }
    public int getSpeed() { return calculateStatWithNature(this.speed, "SPEED"); }
    public PokemonType getType1() { return type1; }
    public PokemonType getType2() { return type2; }
    public PokemonNature getNature() { return nature; }
    public int getMetLevel() { return metLevel; }
    public String getMetMap() { return metMap; }
    public Pokeball getPokeball() { return pokeball; }
    public String getOriginalTrainerName() { return originalTrainerName; }
    public Ability getAbility() { return ability; }
    public Item getHeldItem() { return heldItem; }
    public int getExperiencePoints() { return experiencePoints; }
    public int getExperienceToNextLevel() { return experienceToNextLevel; }
    public Array<Move> getMoves() { return moves; } // Getter para os movimentos

    public String getIconSpriteName() {
        return speciesName.toLowerCase().replace(" ", "");
    }

    public String getFrontSpriteGifPath() {
        String baseName = speciesName.toLowerCase().replace(" ", "");
        String shinySuffix = shiny ? "_shiny" : "";
        return "sprites/pokemons/frontal/" + baseName + shinySuffix + ".gif";
    }
}