package com.mycompany.heromarsspring.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mycompany.heromarsspring.daos.HeroRepository;
import com.mycompany.heromarsspring.daos.SkillRepository;
import com.mycompany.heromarsspring.entities.Hero;
import com.mycompany.heromarsspring.entities.Item;
import com.mycompany.heromarsspring.entities.Skill;
import com.mycompany.heromarsspring.exceptions.InsufficientActionPointsException;
import com.mycompany.heromarsspring.model.ItemEnum;
import com.mycompany.heromarsspring.model.SkillEnum;

@Service
public class HeroActionService {

	@Autowired
	private HeroRepository heroRepository;

	@Autowired
	private SkillRepository skillRepository;

	public static final int TREASUREHUNT_SUCCESS_RATE_LOW = 1;
	public static final int TREASUREHUNT_SUCCESS_RATE_MEDIUM = 2;
	public static final int TREASUREHUNT_SUCCESS_RATE_HIGH = 3;
	public static final int DEFAULT_AMOUNT_OF_MONEY_ON_TREASUREHUNT = 10;

	public int getWaterCost() {
		return 2;
	}

	public int getBlacksmithVisitingCost() {
		return 2;
	}

	public int getAdventureCost() {
		return 2;
	}

	public int getPadavanLearningCost() {
		return 2;
	}

	public int getMasterLearningCost() {
		return 3;
	}

	public int getMageLearningCost() {
		return 5;
	}

	public int getHuntingCost() {
		return 2;
	}

	public int getTreasureHuntingCost() {
		return 2;
	}

	public void decreaseActionPoints(Hero hero, int decreaseAmount) throws InsufficientActionPointsException {

		if (hero.getActionPoint() >= decreaseAmount) {
			hero.setActionPoint(hero.getActionPoint() - decreaseAmount);
			hero.setLastActivity(LocalDateTime.now());
		} else {
			throw new InsufficientActionPointsException();
		}
	}

	public int getWisdomModificationRate(String heroName) {
		int currentWisdom = heroRepository.findByHeroName(heroName).getWisdom();
		return (int) (Math.random() * ((currentWisdom + 3) - (currentWisdom - 3) + 1) + (currentWisdom - 3));
	}

	public String gatherWater(String heroName) throws InsufficientActionPointsException {
		int waterGathered;
		Hero hero = heroRepository.findByHeroName(heroName);

		decreaseActionPoints(hero, getWaterCost());

		List<String> heroSkills = getStringifiedHeroSkillList(heroName);

		if (heroSkills.contains(SkillEnum.WELLDRILLING_MAGE.getDescription())) {
			waterGathered = (int) (SkillEnum.WELLDRILLING_MAGE.getSuccessRate() * getWisdomModificationRate(heroName)
					* 10);
		} else if (heroSkills.contains(SkillEnum.WELLDRILLING_MASTER.getDescription())) {
			waterGathered = (int) (SkillEnum.WELLDRILLING_MASTER.getSuccessRate() * getWisdomModificationRate(heroName)
					* 10);
		} else if (heroSkills.contains(SkillEnum.WELLDRILLING_PADAVAN.getDescription())) {
			waterGathered = (int) (SkillEnum.WELLDRILLING_PADAVAN.getSuccessRate() * getWisdomModificationRate(heroName)
					* 10);
		} else {
			waterGathered = (int) (0.1 * getWisdomModificationRate(heroName) * 10);
		}

		hero.setWater(hero.getWater() + waterGathered);
		heroRepository.saveAndFlush(hero);
		return waterGathered + " vizet sikerült szerezned.";

	}

	public String gatherFood(String heroName) throws InsufficientActionPointsException {
		int foodGathered;
		Hero hero = heroRepository.findByHeroName(heroName);

		decreaseActionPoints(hero, getHuntingCost());

		List<String> heroSkills = getStringifiedHeroSkillList(heroName);

		if (heroSkills.contains(SkillEnum.HUNTER_MAGE.getDescription())) {
			foodGathered = (int) (SkillEnum.HUNTER_MAGE.getSuccessRate() * getWisdomModificationRate(heroName) * 10);
		} else if (heroSkills.contains(SkillEnum.HUNTER_MASTER.getDescription())) {
			foodGathered = (int) (SkillEnum.HUNTER_MASTER.getSuccessRate() * getWisdomModificationRate(heroName) * 10);
		} else if (heroSkills.contains(SkillEnum.HUNTER_PADAVAN.getDescription())) {
			foodGathered = (int) (SkillEnum.HUNTER_PADAVAN.getSuccessRate() * getWisdomModificationRate(heroName) * 10);
		} else {
			foodGathered = (int) (0.1 * getWisdomModificationRate(heroName) * 10);
		}

		hero.setFood(hero.getFood() + foodGathered);
		heroRepository.saveAndFlush(hero);
		return foodGathered + " kaját sikerült szerezned.";
	}

	public String getTreasures(String heroName) throws InsufficientActionPointsException {

		Hero hero = heroRepository.findByHeroName(heroName);
		int treasuresGatheredOnTreasureHunt = getMoneyAsTreasureHuntingReward(hero.getHeroName());

		decreaseActionPoints(hero, getTreasureHuntingCost());
		hero.setMoney(hero.getMoney() + treasuresGatheredOnTreasureHunt);

		heroRepository.saveAndFlush(hero);

		return treasuresGatheredOnTreasureHunt + " pénzt sikerült szerezned.";
	}

	public String goToAnAdvanture(String heroName) throws InsufficientActionPointsException {
		Item item;
		Hero hero = heroRepository.findByHeroName(heroName);
		double treasureHuntingSuccessRate = getTreasureHuntingSuccesRate(heroName);

		decreaseActionPoints(hero, getTreasureHuntingCost());

		if (treasureHuntingSuccessRate > TREASUREHUNT_SUCCESS_RATE_HIGH) {
			item = getItemAsAdvantureReward(ItemEnum.MAGIC_RING, 3);
		} else if (treasureHuntingSuccessRate > TREASUREHUNT_SUCCESS_RATE_MEDIUM) {
			item = getItemAsAdvantureReward(ItemEnum.LIGHTSWORD, 3);
		} else {
			item = getItemAsAdvantureReward(ItemEnum.HELMET, 3);
		}

		item.setHero(hero);
		hero.getItems().add(item);

		heroRepository.saveAndFlush(hero);

		return "A következő eszközt szerezted meg: " + item.getName().getDescription();
	}

	public String developWelldrillingSkill(String heroName) throws InsufficientActionPointsException {
		List<String> heroSkills = getStringifiedHeroSkillList(heroName);
		Hero hero = heroRepository.findByHeroName(heroName);

		if (heroSkills.contains(SkillEnum.WELLDRILLING_MAGE.getDescription())) {
			return "Már maximumra fejlesztetted ezt a képességet.";
		}

		Skill skill;
		SkillEnum skillToGain = defineNextLevelOfHerosWelldrillingSkill(heroSkills, hero);

		skill = setNewSkillFromDbIfExists(skillToGain);
		setNewValuesForGlobalHeroAttributesBySkillModificationValues(hero, skillToGain);
		saveNewHeroSkill(hero, skill);

		return skill.getSkillType().getDescription() + " képességet sikerült szerezned.";
	}

	private SkillEnum defineNextLevelOfHerosWelldrillingSkill(List<String> heroSkills, Hero hero)
			throws InsufficientActionPointsException {
		SkillEnum skillToGain;

		if (heroSkills.contains(SkillEnum.WELLDRILLING_MASTER.getDescription())) {
			skillToGain = SkillEnum.WELLDRILLING_MAGE;
			hero.getSkills().remove(skillRepository.findBySkillType(SkillEnum.WELLDRILLING_MASTER));
			decreaseActionPoints(hero, getMageLearningCost());
		} else if (heroSkills.contains(SkillEnum.WELLDRILLING_PADAVAN.getDescription())) {
			skillToGain = SkillEnum.WELLDRILLING_MASTER;
			hero.getSkills().remove(skillRepository.findBySkillType(SkillEnum.WELLDRILLING_PADAVAN));
			decreaseActionPoints(hero, getMasterLearningCost());
		} else {
			skillToGain = SkillEnum.WELLDRILLING_PADAVAN;
			decreaseActionPoints(hero, getPadavanLearningCost());
		}
		return skillToGain;
	}

	public String developHuntingSkill(String heroName) throws InsufficientActionPointsException {
		List<String> heroSkills = getStringifiedHeroSkillList(heroName);
		Hero hero = heroRepository.findByHeroName(heroName);

		if (heroSkills.contains(SkillEnum.HUNTER_MAGE.getDescription())) {
			return "Már maximumra fejlesztetted ezt a képességet.";
		}

		SkillEnum skillToGain = defineNextLevelOfHerosHunterSkill(heroSkills, hero);
		Skill skill = setNewSkillFromDbIfExists(skillToGain);

		setNewValuesForGlobalHeroAttributesBySkillModificationValues(hero, skillToGain);
		saveNewHeroSkill(hero, skill);
		return skill.getSkillType().getDescription() + " képességet sikerült szerezned.";
	}

	private SkillEnum defineNextLevelOfHerosHunterSkill(List<String> heroSkills, Hero hero)
			throws InsufficientActionPointsException {
		SkillEnum skillToGain;

		if (heroSkills.contains(SkillEnum.HUNTER_MASTER.getDescription())) {
			skillToGain = SkillEnum.HUNTER_MAGE;
			hero.getSkills().remove(skillRepository.findBySkillType(SkillEnum.HUNTER_MASTER));
			decreaseActionPoints(hero, getMageLearningCost());
		} else if (heroSkills.contains(SkillEnum.HUNTER_PADAVAN.getDescription())) {
			skillToGain = SkillEnum.HUNTER_MASTER;
			hero.getSkills().remove(skillRepository.findBySkillType(SkillEnum.HUNTER_PADAVAN));
			decreaseActionPoints(hero, getMasterLearningCost());
		} else {
			skillToGain = SkillEnum.HUNTER_PADAVAN;
			decreaseActionPoints(hero, getPadavanLearningCost());
		}
		return skillToGain;
	}

	public String developTreasureHunterSkill(String heroName) throws InsufficientActionPointsException {
		List<String> heroSkills = getStringifiedHeroSkillList(heroName);
		Hero hero = heroRepository.findByHeroName(heroName);

		if (heroSkills.contains(SkillEnum.TREASURE_HUNTER_MAGE.getDescription())) {
			return "Már maximumra fejlesztetted ezt a képességet.";
		}

		Skill skill;
		SkillEnum skillToGain = defineNextLevelOfHerosTreasureHunterSkill(heroSkills, hero);

		skill = setNewSkillFromDbIfExists(skillToGain);
		setNewValuesForGlobalHeroAttributesBySkillModificationValues(hero, skillToGain);
		saveNewHeroSkill(hero, skill);

		return skill.getSkillType().getDescription() + " képességet sikerült szerezned.";
	}

	private SkillEnum defineNextLevelOfHerosTreasureHunterSkill(List<String> heroSkills, Hero hero)
			throws InsufficientActionPointsException {
		SkillEnum skillToGain;

		if (heroSkills.contains(SkillEnum.TREASURE_HUNTER_MASTER.getDescription())) {
			skillToGain = SkillEnum.TREASURE_HUNTER_MAGE;
			hero.getSkills().remove(skillRepository.findBySkillType(SkillEnum.TREASURE_HUNTER_MASTER));
			decreaseActionPoints(hero, getMageLearningCost());
		} else if (heroSkills.contains(SkillEnum.TREASURE_HUNTER_PADAVAN.getDescription())) {
			skillToGain = SkillEnum.TREASURE_HUNTER_MASTER;
			hero.getSkills().remove(skillRepository.findBySkillType(SkillEnum.TREASURE_HUNTER_PADAVAN));
			decreaseActionPoints(hero, getMasterLearningCost());
		} else {
			skillToGain = SkillEnum.TREASURE_HUNTER_PADAVAN;
			decreaseActionPoints(hero, getPadavanLearningCost());
		}
		return skillToGain;
	}

	public String developAstronomerSkill(String heroName) throws InsufficientActionPointsException {
		List<String> heroSkills = getStringifiedHeroSkillList(heroName);
		Hero hero = heroRepository.findByHeroName(heroName);

		if (heroSkills.contains(SkillEnum.ASTRONOMER_MAGE.getDescription())) {
			return "Már maximumra fejlesztetted ezt a képességet.";
		}

		Skill skill;
		SkillEnum skillToGain = defineNextLevelOfHerosAstronomerSkill(heroSkills, hero);

		skill = setNewSkillFromDbIfExists(skillToGain);
		setNewValuesForGlobalHeroAttributesBySkillModificationValues(hero, skillToGain);
		saveNewHeroSkill(hero, skill);

		return skill.getSkillType().getDescription() + " képességet sikerült szerezned.";
	}

	private SkillEnum defineNextLevelOfHerosAstronomerSkill(List<String> heroSkills, Hero hero)
			throws InsufficientActionPointsException {
		SkillEnum skillToGain;

		if (heroSkills.contains(SkillEnum.ASTRONOMER_MASTER.getDescription())) {
			skillToGain = SkillEnum.ASTRONOMER_MAGE;
			hero.getSkills().remove(skillRepository.findBySkillType(SkillEnum.ASTRONOMER_MASTER));
			decreaseActionPoints(hero, getMageLearningCost());
		} else if (heroSkills.contains(SkillEnum.ASTRONOMER_PADAVAN.getDescription())) {
			skillToGain = SkillEnum.ASTRONOMER_MASTER;
			hero.getSkills().remove(skillRepository.findBySkillType(SkillEnum.ASTRONOMER_PADAVAN));
			decreaseActionPoints(hero, getMasterLearningCost());
		} else {
			skillToGain = SkillEnum.ASTRONOMER_PADAVAN;
			decreaseActionPoints(hero, getPadavanLearningCost());
		}
		return skillToGain;
	}

	private void saveNewHeroSkill(Hero hero, Skill skill) {
		skill.getHeroes().add(hero);
		hero.getSkills().add(skill);

		skillRepository.saveAndFlush(skill);
		heroRepository.saveAndFlush(hero);
	}

	/*
	 * This is the way in which we can evaluate, if a skill is already existing in
	 * the database. If it is not there yet, we are placing it, otherwise we set
	 * that skill for the hero.
	 */
	private Skill setNewSkillFromDbIfExists(SkillEnum skillToGain) {

		Skill skill;
		Skill existingSkillInDb = skillRepository.findBySkillType(skillToGain);
		skill = getSkillAsLearningReward(skillToGain);

		if (existingSkillInDb != null) {
			skill = existingSkillInDb;
		}
		return skill;
	}

	public double getTreasureHuntingSuccesRate(String heroName) {

		List<String> heroSkills = getStringifiedHeroSkillList(heroName);

		if (heroSkills.contains(SkillEnum.TREASURE_HUNTER_MAGE.getDescription())) {
			return SkillEnum.TREASURE_HUNTER_MAGE.getSuccessRate() * getWisdomModificationRate(heroName);
		} else if (heroSkills.contains(SkillEnum.TREASURE_HUNTER_MASTER.getDescription())) {
			return SkillEnum.TREASURE_HUNTER_MASTER.getSuccessRate() * getWisdomModificationRate(heroName);
		} else if (heroSkills.contains(SkillEnum.TREASURE_HUNTER_PADAVAN.getDescription())) {
			return SkillEnum.TREASURE_HUNTER_PADAVAN.getSuccessRate() * getWisdomModificationRate(heroName);
		} else {
			return 0.1 * getWisdomModificationRate(heroName);
		}
	}

	public List<String> getStringifiedHeroSkillList(String heroName) {

		List<String> heroSkills = heroRepository.findByHeroName(heroName).getSkills().stream()
				.map(s -> s.getSkillType().getDescription()).collect(Collectors.toList());
		return heroSkills;
	}

	public Item getItemAsAdvantureReward(ItemEnum type, int level) {
		Item item = new Item();
		item.setName(type);
		item.setLevel(level);
		item.setIsInUse(false);
		item.setDurability(item.getName().getDurability());
		item.setItemHpMod(item.getName().getHealthMod() * item.getLevel());
		item.setItemStrengthMod(item.getName().getStrengthMod() * item.getLevel());
		item.setItemWisdomMod(item.getName().getWisdomMod() * item.getLevel());
		item.setType(item.getName().getType());
		item.setMarketPresence(null);
		return item;
	}

	public Skill getSkillAsLearningReward(SkillEnum skillType) {
		Skill skill = new Skill();
		skill.setSkillType(skillType);
		return skill;
	}

	public int getMoneyAsTreasureHuntingReward(String heroName) {
		int additionalMoneyOnTreasureHunt = (int) getTreasureHuntingSuccesRate(heroName);
		return DEFAULT_AMOUNT_OF_MONEY_ON_TREASUREHUNT + additionalMoneyOnTreasureHunt;
	}

	public void setNewValuesForGlobalHeroAttributesBySkillModificationValues(Hero hero, SkillEnum skillGained) {
		hero.setWisdom(hero.getWisdom() + skillGained.getSkillWisdomMod());
		hero.setStrength(hero.getStrength() + skillGained.getSkillStrengthMod());
		hero.setHp(hero.getHp() + skillGained.getSkillHpMod());
	}
}
