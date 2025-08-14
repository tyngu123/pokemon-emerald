package br.com.yago.emerald.model;

import com.badlogic.gdx.utils.Array;
import br.com.yago.emerald.database.PokemonData;
import java.util.Map;

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
    private final Array<Move> moves;

    // Construtor original, mantido para compatibilidade
    public Pokemon(
        PokemonData data, int level, String nickname, Gender gender, boolean shiny,
        PokemonNature nature, int metLevel, String metMap, Pokeball pokeball, 
        String originalTrainerName, Ability ability
    ) {
        this(data, nickname, gender, shiny, nature, metMap, pokeball, originalTrainerName, ability, ExperienceCalculator.getTotalExpForLevel(level, ExperienceGroup.valueOf(data.experienceGroup)));
    }

    // Novo construtor para criação com pontos de experiência específicos
    public Pokemon(
        PokemonData data, String nickname, Gender gender, boolean shiny,
        PokemonNature nature, String metMap, Pokeball pokeball, 
        String originalTrainerName, Ability ability, int experiencePoints
    ) {
        this.speciesName = data.speciesName;
        this.nickname = nickname;
        this.gender = gender;
        this.shiny = shiny;
        this.nature = nature;
        this.metMap = metMap;
        this.pokeball = pokeball;
        this.originalTrainerName = originalTrainerName;
        this.ability = ability;
        this.heldItem = null;
        
        this.type1 = PokemonType.valueOf(data.type1);
        this.type2 = (data.type2 != null) ? PokemonType.valueOf(data.type2) : null;
        this.experienceGroup = ExperienceGroup.valueOf(data.experienceGroup);
        
        this.experiencePoints = experiencePoints;
        this.level = ExperienceCalculator.calculateLevelFromExp(this.experiencePoints, this.experienceGroup);
        this.metLevel = this.level;
        
        this.maxHP = StatCalculator.calculateHP(data.baseStats.get("HP"), this.level);
        this.attack = StatCalculator.calculateStat(data.baseStats.get("ATTACK"), this.level);
        this.defense = StatCalculator.calculateStat(data.baseStats.get("DEFENSE"), this.level);
        this.specialAttack = StatCalculator.calculateStat(data.baseStats.get("SPECIAL_ATTACK"), this.level);
        this.specialDefense = StatCalculator.calculateStat(data.baseStats.get("SPECIAL_DEFENSE"), this.level);
        this.speed = StatCalculator.calculateStat(data.baseStats.get("SPEED"), this.level);
        
        this.currentHP = this.maxHP;
        this.experienceToNextLevel = ExperienceCalculator.getExpToNextLevel(this.level, this.experienceGroup);
        
        this.moves = new Array<>(4);
    }

    public void learnMove(Move move) {
        if (moves.size < 4) {
            moves.add(move);
        }
    }
    
    /**
     * ## NOVO MÉTODO ##
     * Aplica dano ao Pokémon, garantindo que o HP não fique negativo.
     * @param damage A quantidade de dano a ser aplicada.
     */
    public void takeDamage(int damage) {
        this.currentHP -= damage;
        if (this.currentHP < 0) {
            this.currentHP = 0;
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
    public ExperienceGroup getExperienceGroup() { return experienceGroup; }
    public Array<Move> getMoves() { return moves; }

    public String getIconSpriteName() {
        return speciesName.toLowerCase().replace(" ", "");
    }

    public String getFrontSpriteGifPath() {
        String baseName = speciesName.toLowerCase().replace(" ", "");
        String shinySuffix = shiny ? "_shiny" : "";
        return "sprites/pokemons/frontal/" + baseName + shinySuffix + ".gif";
    }
    
    public String getBackSpriteGifPath() {
        String baseName = speciesName.toLowerCase().replace(" ", "");
        String shinySuffix = shiny ? "_shiny" : "";
        return "sprites/pokemons/back/" + baseName + shinySuffix + ".gif";
    }
}