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

    public Pokemon(String speciesName, String nickname, Gender gender, boolean shiny, int level,
                   int maxHP, int attack, int defense, int specialAttack, int specialDefense, int speed) {
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
    }

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
     * ## MÉTODO ATUALIZADO ##
     * Retorna o nome do sprite do ícone. A verificação de shiny foi removida
     * para que ambos, shiny e normal, usem o mesmo ícone.
     */
    public String getIconSpriteName() {
        return speciesName.toLowerCase().replace(" ", "");
    }

    /**
     * Retorna o caminho para o GIF do sprite frontal.
     * Adiciona "_shiny" ao nome do arquivo se o Pokémon for shiny.
     * Ex.: "sprites/pokemons/frontal/mudkip_shiny.gif"
     */
    public String getFrontSpriteGifPath() {
        String baseName = speciesName.toLowerCase().replace(" ", "");
        String shinySuffix = shiny ? "_shiny" : "";
        return "sprites/pokemons/frontal/" + baseName + shinySuffix + ".gif";
    }
}