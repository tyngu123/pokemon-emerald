package br.com.yago.emerald.model;

public class Pokemon {

    public enum Gender { MALE, FEMALE, GENDERLESS }

    // Identificação
    private String speciesName;   // Nome da espécie (ex.: "Pikachu")
    private String nickname;      // Apelido dado pelo jogador
    private Gender gender;
    private boolean shiny;

    // Status de batalha
    private int level;
    private int currentHP;
    private int maxHP;
    private int attack;
    private int defense;
    private int specialAttack;
    private int specialDefense;
    private int speed;

    // Atributos base (para futuros cálculos)
    private int baseHP;
    private int baseAttack;
    private int baseDefense;
    private int baseSpecialAttack;
    private int baseSpecialDefense;
    private int baseSpeed;

    public Pokemon(String speciesName, String nickname, Gender gender, boolean shiny, int level,
                   int maxHP, int attack, int defense, int specialAttack, int specialDefense, int speed) {
        this.speciesName = speciesName;
        this.nickname = nickname;
        this.gender = gender;
        this.shiny = shiny;
        this.level = level;
        this.maxHP = maxHP;
        this.currentHP = maxHP; // Começa com HP cheio
        this.attack = attack;
        this.defense = defense;
        this.specialAttack = specialAttack;
        this.specialDefense = specialDefense;
        this.speed = speed;
    }
    
    // Getters para todos os atributos
    public String getSpeciesName() { return speciesName; }
    public String getNickname() { return nickname; }
    public Gender getGender() { return gender; }
    public boolean isShiny() { return shiny; }
    public int getLevel() { return level; }
    public int getCurrentHP() { return currentHP; }
    public int getMaxHP() { return maxHP; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getSpecialAttack() { return specialAttack; }
    public int getSpecialDefense() { return specialDefense; }
    public int getSpeed() { return speed; }

    /**
     * Retorna o nome do sprite do ícone, considerando se é shiny.
     * Ex: "mudkip" ou "mudkip_shiny"
     */
    public String getIconSpriteName() {
        return speciesName.toLowerCase().replace(" ", "") + (shiny ? "_shiny" : "");
    }
}