package br.com.yago.emerald.model;

public class Pokemon {

    public enum Gender { MALE, FEMALE, GENDERLESS }

    private String speciesName;
    private String nickname;
    private Gender gender;
    private boolean shiny;
    private int level;
    private int currentHP;
    private int maxHP;
    private int attack;
    private int defense;
    private int specialAttack;
    private int specialDefense;
    private int speed;
    private int baseHP;
    private int baseAttack;
    private int baseDefense;
    private int baseSpecialAttack;
    private int baseSpecialDefense;
    private int baseSpeed;

    private final PokemonType type1;
    private final PokemonType type2;

    private final PokemonNature nature;
    private final int metLevel;
    private final String metMap;
    
    private final Pokeball pokeball;
    private final String originalTrainerName;

    // ## NOVO CAMPO ADICIONADO ##
    private final Ability ability;

    public Pokemon(String speciesName, String nickname, Gender gender, boolean shiny, int level,
                   int maxHP, int attack, int defense, int specialAttack, int specialDefense, int speed,
                   PokemonType type1, PokemonType type2, PokemonNature nature, int metLevel, String metMap,
                   Pokeball pokeball, String originalTrainerName, Ability ability) { // ## CONSTRUTOR ATUALIZADO ##
        this.speciesName = speciesName;
        this.nickname = nickname;
        this.gender = gender;
        this.shiny = shiny;
        this.level = level;
        this.maxHP = maxHP;
        this.currentHP = maxHP;
        this.attack = attack;
        this.defense = defense;
        this.specialAttack = specialAttack;
        this.specialDefense = specialDefense;
        this.speed = speed;
        this.type1 = type1;
        this.type2 = type2;
        this.nature = nature;
        this.metLevel = metLevel;
        this.metMap = metMap;
        this.pokeball = pokeball;
        this.originalTrainerName = originalTrainerName;
        this.ability = ability; // ## INICIALIZAÇÃO DO NOVO CAMPO ##
    }

    // Getters
    public String getSpeciesName() { return speciesName; }
    public String getNickname() { return nickname; }
    public Gender getGender() { return gender; }
    public boolean isShiny() { return shiny; }
    public int getLevel() { return level; }
    public int getCurrentHP() { return currentHP; }
    public int getMaxHP() { return maxHP; }
    public PokemonType getType1() { return type1; }
    public PokemonType getType2() { return type2; }
    public PokemonNature getNature() { return nature; }
    public int getMetLevel() { return metLevel; }
    public String getMetMap() { return metMap; }
    public Pokeball getPokeball() { return pokeball; }
    public String getOriginalTrainerName() { return originalTrainerName; }
    
    // ## GETTER PARA O NOVO CAMPO ##
    public Ability getAbility() { return ability; }

    public String getIconSpriteName() {
        return speciesName.toLowerCase().replace(" ", "");
    }

    public String getFrontSpriteGifPath() {
        String baseName = speciesName.toLowerCase().replace(" ", "");
        String shinySuffix = shiny ? "_shiny" : "";
        return "sprites/pokemons/frontal/" + baseName + shinySuffix + ".gif";
    }
}