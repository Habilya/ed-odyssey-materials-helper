package nl.jixxed.eliteodysseymaterials.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;
import nl.jixxed.eliteodysseymaterials.constants.OdysseyBlueprintConstants;
import nl.jixxed.eliteodysseymaterials.constants.PreferenceConstants;
import nl.jixxed.eliteodysseymaterials.enums.*;
import nl.jixxed.eliteodysseymaterials.helper.WishlistHelper;
import nl.jixxed.eliteodysseymaterials.service.PreferencesService;
import nl.jixxed.eliteodysseymaterials.service.StorageService;
import nl.jixxed.eliteodysseymaterials.service.event.*;
import nl.jixxed.eliteodysseymaterials.service.event.trade.EnlistWebSocketEvent;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Slf4j
public class ApplicationState {

    private static ApplicationState applicationState;
    private final Function<WishlistBlueprint, String> wishlistRecipeMapper = recipe -> ((OdysseyBlueprintName) recipe.getRecipeName()).name() + ":" + recipe.isVisible();
    private final List<OdysseyMaterial> favourites = new ArrayList<>();
    private final Set<Commander> commanders = new HashSet<>();
    private final Map<Engineer, EngineerStatus> engineerStates = new EnumMap<>(Map.ofEntries(
            Map.entry(Engineer.DOMINO_GREEN, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.HERO_FERRARI, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.JUDE_NAVARRO, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.KIT_FOWLER, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.ODEN_GEIGER, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.TERRA_VELASQUEZ, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.UMA_LASZLO, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.WELLINGTON_BECK, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.YARDEN_BOND, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.BALTANOS, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.ELEANOR_BRESA, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.ROSA_DAYETTE, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.YI_SHEN, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.COLONEL_BRIS_DEKKER, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.MARCO_QWENT, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.THE_DWELLER, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.LORI_JAMESON, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.THE_SARGE, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.SELENE_JEAN, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.LIZ_RYDER, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.BILL_TURNER, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.TOD_THE_BLASTER_MCQUINN, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.PROFESSOR_PALIN, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.DIDI_VATERMANN, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.JURI_ISHMAAK, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.LEI_CHEUNG, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.FELICITY_FARSEER, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.TIANA_FORTUNE, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.ZACARIAH_NEMO, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.RAM_TAH, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.BROO_TARQUIN, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.ELVIRA_MARTUUK, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.HERA_TANI, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.CHLOE_SEDESI, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.MARSHA_HICKS, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.ETIENNE_DORN, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.MEL_BRANDON, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.PETRA_OLMANOVA, new EngineerStatus(EngineerState.UNKNOWN, 0, 0)),
            Map.entry(Engineer.UNKNOWN, new EngineerStatus(EngineerState.UNKNOWN, 0, 0))
    ));
    private GameMode gameMode = GameMode.NONE;

    private ApplicationState() {
        final String fav = PreferencesService.getPreference("material.favourites", "");
        Arrays.stream(fav.split(","))
                .filter(material -> !material.isBlank())
                .map(OdysseyMaterial::subtypeForName)
                .forEach(this.favourites::add);

        EventService.addListener(this, 0, WishlistBlueprintEvent.class,
                wishlistEvent -> Platform.runLater(() ->
                        wishlistEvent.getWishlistBlueprints().forEach(wishlistRecipe -> {
                            switch (wishlistEvent.getAction()) {
                                case ADDED -> addToWishList(wishlistEvent.getWishlistUUID(), wishlistEvent.getFid(), wishlistRecipe.getRecipeName());
                                case REMOVED -> removeFromWishList(wishlistEvent.getWishlistUUID(), wishlistEvent.getFid(), wishlistRecipe);
                                case VISIBILITY_CHANGED -> changeVisibility(wishlistEvent.getWishlistUUID(), wishlistEvent.getFid(), wishlistRecipe);
                            }
                        })));

        EventService.addListener(this, EnlistWebSocketEvent.class, event -> getPreferredCommander().ifPresent(commander -> PreferencesService.setPreference(PreferenceConstants.MARKETPLACE_TOKEN_PREFIX + commander.getFid(), event.getEnlistMessage().getTrace().getToken())));
        EventService.addListener(this, LoadGameEvent.class, event -> this.gameMode = event.getGameMode());
    }

    public static ApplicationState getInstance() {
        if (applicationState == null) {
            applicationState = new ApplicationState();
        }
        return applicationState;
    }

    public GameMode getGameMode() {
        return this.gameMode;
    }

    public boolean getSoloMode() {
        return PreferencesService.getPreference(PreferenceConstants.SOLO_MODE, Boolean.FALSE);
    }

    public boolean isEngineerKnown(final Engineer engineer) {
        final EngineerState engineerState = this.engineerStates.get(engineer).getEngineerState();
        return EngineerState.KNOWN.equals(engineerState) || isEngineerUnlocked(engineer);

    }

    public boolean isEngineerUnlocked(final Engineer engineer) {
        final EngineerState engineerState = this.engineerStates.get(engineer).getEngineerState();
        return EngineerState.INVITED.equals(engineerState) || EngineerState.UNLOCKED.equals(engineerState);
    }

    public boolean isEngineerInvited(final Engineer engineer) {
        final EngineerState engineerState = this.engineerStates.get(engineer).getEngineerState();
        return EngineerState.INVITED.equals(engineerState);
    }

    public boolean isEngineerUnlockedExact(final Engineer engineer) {
        final EngineerState engineerState = this.engineerStates.get(engineer).getEngineerState();
        return EngineerState.UNLOCKED.equals(engineerState);
    }

    public void setEngineerStatus(final Engineer engineer, final EngineerState engineerState, final Integer rank, final Integer progress) {
        final EngineerStatus engineerStatus = this.engineerStates.get(engineer);
        engineerStatus.setEngineerState(engineerState);
        engineerStatus.setRank(rank);
        engineerStatus.setProgress(progress);
    }

    public void setEngineerState(final Engineer engineer, final EngineerState engineerState) {
        this.engineerStates.get(engineer).setEngineerState(engineerState);
    }

    public void setEngineerRank(final Engineer engineer, final Integer rank) {
        this.engineerStates.get(engineer).setRank(rank);
    }

    public Integer getEngineerRank(final Engineer engineer) {
        return this.engineerStates.get(engineer).getRank();
    }

    public void setEngineerProgress(final Engineer engineer, final Integer progress) {
        this.engineerStates.get(engineer).setProgress(progress);
    }

    public Integer getEngineerProgress(final Engineer engineer) {
        return this.engineerStates.get(engineer).getProgress();
    }


    public void resetEngineerStates() {
        this.engineerStates.forEach((engineer, engineerState) -> {
            this.engineerStates.get(engineer).setEngineerState(EngineerState.UNKNOWN);
            this.engineerStates.get(engineer).setProgress(0);
            this.engineerStates.get(engineer).setRank(0);
        });
        EventService.publish(new EngineerEvent());
    }

    public <T extends OdysseyMaterial> boolean toggleFavourite(final T material) {
        final boolean newState;
        if (this.favourites.contains(material)) {
            this.favourites.remove(material);
            newState = false;
        } else {
            this.favourites.add(material);
            newState = true;
        }
        PreferencesService.setPreference("material.favourites", this.favourites, OdysseyMaterial::name);
        return newState;
    }

    public boolean isFavourite(final OdysseyMaterial odysseyMaterial) {
        return this.favourites.contains(odysseyMaterial);
    }

    private void addToWishList(final String wishlistUUID, final String fid, final BlueprintName recipe) {
        final Wishlists wishlists = getWishlists(fid);
        final Wishlist wishlist = wishlists.getWishlist(wishlistUUID);
        wishlist.getItems().add(new WishlistBlueprint((OdysseyBlueprintName) recipe, true));
        saveWishlists(fid, wishlists);
        EventService.publish(new WishlistChangedEvent(wishlistUUID));
    }

    private void removeFromWishList(final String wishlistUUID, final String fid, final WishlistBlueprint recipe) {
        final Wishlists wishlists = getWishlists(fid);
        final Wishlist wishlist = wishlists.getWishlist(wishlistUUID);
        final Optional<WishlistBlueprint> found = wishlist.getItems().stream().filter(wishlistRecipe -> wishlistRecipe.equals(recipe)).findFirst();
        found.ifPresent(wishlistRecipe -> wishlist.getItems().remove(wishlistRecipe));
        saveWishlists(fid, wishlists);
        EventService.publish(new WishlistChangedEvent(wishlistUUID));
    }

    private void changeVisibility(final String wishlistUUID, final String fid, final WishlistBlueprint wishlistBlueprint) {
        final Wishlists wishlists = getWishlists(fid);
        final Wishlist wishlist = wishlists.getWishlist(wishlistUUID);
        final Optional<WishlistBlueprint> existingRecipe = wishlist.getItems().stream().filter(recipe -> recipe.getRecipeName().equals(wishlistBlueprint.getRecipeName()) && recipe.isVisible() == !wishlistBlueprint.isVisible()).findFirst();
        existingRecipe.ifPresent(recipe -> recipe.setVisible(wishlistBlueprint.isVisible()));
        saveWishlists(fid, wishlists);
        EventService.publish(new WishlistChangedEvent(wishlistUUID));
    }

    public void selectWishlist(final String wishlistUUID, final String fid) {
        final Wishlists wishlists = getWishlists(fid);
        wishlists.setSelectedWishlistUUID(wishlistUUID);
        saveWishlists(fid, wishlists);
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public Wishlists getWishlists(final String fid) {
        final String wishlists = PreferencesService.getPreference(PreferenceConstants.WISHLISTS_PREFIX + fid, "N/A");
        try {
            if (wishlists.equals("")) {
                return OBJECT_MAPPER.readValue(getOldStyleWishList2(fid), Wishlists.class);
            } else if (!wishlists.equals("N/A")) {
                return OBJECT_MAPPER.readValue(wishlists, Wishlists.class);
            } else {
                return OBJECT_MAPPER.readValue(getOldStyleWishList2(fid), Wishlists.class);
            }
        } catch (final JsonProcessingException e) {
            log.error("Failed to load wishlists", e);
        }
        throw new IllegalStateException("Unable to load wishlists from configuration.");
    }

    public void saveWishlists(final String fid, final Wishlists wishlists) {
        try {
            PreferencesService.setPreference(PreferenceConstants.WISHLISTS_PREFIX + fid, OBJECT_MAPPER.writeValueAsString(wishlists));
        } catch (final JsonProcessingException e) {
            log.error("Failed to save wishlists", e);
        }
    }

    public void deleteWishlist(final String activeWishlistUUID, final String fid) {
        final Wishlists wishlists = getWishlists(fid);
        wishlists.delete(activeWishlistUUID);
        saveWishlists(fid, wishlists);
    }

    private String getOldStyleWishList2(final String fid) {
        final String recipes = PreferencesService.getPreference(PreferenceConstants.WISHLIST_RECIPES_PREFIX + fid, "");
        //transfer old style to new style
        final Wishlists wishlists = new Wishlists();
        final Wishlist defaultWishlist = new Wishlist();
        defaultWishlist.setName("Default wishlist");
        defaultWishlist.setItems(parseFIDWishlist(recipes));
        wishlists.addWishlist(defaultWishlist);
        try {
            PreferencesService.setPreference(PreferenceConstants.WISHLISTS_PREFIX + fid, OBJECT_MAPPER.writeValueAsString(wishlists));
            //reset old style to empty
            PreferencesService.setPreference(PreferenceConstants.WISHLIST_RECIPES_PREFIX, new ArrayList<>(), this.wishlistRecipeMapper);
        } catch (final JsonProcessingException e) {
            log.error("Failed to save wishlists", e);
        }
        return PreferencesService.getPreference(PreferenceConstants.WISHLISTS_PREFIX + fid, "N/A");
    }

    private List<WishlistBlueprint> parseFIDWishlist(final String recipes) {
        return WishlistHelper.convertWishlist(recipes);
    }

    public Set<Commander> getCommanders() {
        return this.commanders;
    }

    public Optional<Commander> getPreferredCommander() {
        final String preferredCommander = PreferencesService.getPreference(PreferenceConstants.COMMANDER, "");
        if (!preferredCommander.isBlank() && this.commanders.stream().anyMatch(commander -> commander.getName().equals(preferredCommander))) {
            return this.commanders.stream().filter(commander -> commander.getName().equals(preferredCommander)).findFirst();
        }
        final Iterator<Commander> commanderIterator = this.commanders.iterator();
        if (commanderIterator.hasNext()) {
            final Commander commander = commanderIterator.next();
            PreferencesService.setPreference(PreferenceConstants.COMMANDER, commander.getName());
            return Optional.of(commander);
        }
        return Optional.empty();
    }

    public void addCommander(final String name, final String fid) {
        if (this.commanders.stream().noneMatch(commander -> commander.getName().equals(name))) {
            final Commander commander = new Commander(name, fid);
            this.commanders.add(commander);
            final String preferredCommander = PreferencesService.getPreference(PreferenceConstants.COMMANDER, "");
            if (preferredCommander.isBlank()) {
                PreferencesService.setPreference(PreferenceConstants.COMMANDER, name);
            }
            EventService.publish(new CommanderAddedEvent(commander));
        }
    }

    public void resetCommanders() {
        this.commanders.clear();
    }

    public int amountCraftable(final OdysseyBlueprintName odysseyBlueprintName) {
        final OdysseyBlueprint blueprint = OdysseyBlueprintConstants.getRecipe(odysseyBlueprintName);
        final AtomicInteger lowestAmount = new AtomicInteger(9999);
        blueprint.getMaterialCollection(OdysseyMaterial.class).forEach((material, amountRequired) -> {
            final int amountCraftable = StorageService.getMaterialStorage(material).getTotalValue() / amountRequired;
            lowestAmount.set(Math.min(amountCraftable, lowestAmount.get()));
        });
        return lowestAmount.get();
    }

    public Craftability getCraftability(final OdysseyBlueprintName odysseyBlueprintName) {
        final OdysseyBlueprint blueprint = OdysseyBlueprintConstants.getRecipe(odysseyBlueprintName);
        final AtomicBoolean hasGoods = new AtomicBoolean(true);
        final AtomicBoolean hasData = new AtomicBoolean(true);
        final AtomicBoolean hasAssets = new AtomicBoolean(true);
        blueprint.getMaterialCollection(Good.class).forEach((material, amountRequired) -> hasGoods.set(hasGoods.get() && (StorageService.getMaterialStorage(material).getTotalValue() - amountRequired) >= 0));
        blueprint.getMaterialCollection(Data.class).forEach((material, amountRequired) -> hasData.set(hasData.get() && (StorageService.getMaterialStorage(material).getTotalValue() - amountRequired) >= 0));
        blueprint.getMaterialCollection(Asset.class).forEach((material, amountRequired) -> hasAssets.set(hasAssets.get() && (StorageService.getMaterialStorage(material).getTotalValue() - amountRequired) >= 0));
        if (!hasGoods.get() || !hasData.get()) {
            return Craftability.NOT_CRAFTABLE;
        } else if (hasGoods.get() && hasData.get() && !hasAssets.get()) {
            return Craftability.CRAFTABLE_WITH_TRADE;
        } else {
            return Craftability.CRAFTABLE;
        }
    }

    public String getMarketPlaceToken() {
        return getPreferredCommander().map(commander -> PreferencesService.getPreference(PreferenceConstants.MARKETPLACE_TOKEN_PREFIX + commander.getFid(), "")).orElse("");
    }

    public LoadoutSetList getLoadoutSetList(final String fid) {
        final String loadoutSetList = PreferencesService.getPreference(PreferenceConstants.LOADOUTS_PREFIX + fid, "N/A");
        try {
            if (!loadoutSetList.equals("N/A")) {
                return OBJECT_MAPPER.readValue(loadoutSetList, LoadoutSetList.class);
            } else {
                return OBJECT_MAPPER.readValue(createLoadoutSetList(fid), LoadoutSetList.class);
            }
        } catch (final JsonProcessingException e) {
            log.error("Failed to load loadouts", e);
        }
        throw new IllegalStateException("Unable to load loadouts from configuration.");
    }

    private String createLoadoutSetList(final String fid) {
        final LoadoutSetList loadoutSetList = new LoadoutSetList();
        final LoadoutSet defaultLoadoutSet = new LoadoutSet();
        defaultLoadoutSet.setName("Default Loadout");
        defaultLoadoutSet.setLoadouts(List.of());
        loadoutSetList.addLoadoutSet(defaultLoadoutSet);
        saveLoadoutSetList(fid, loadoutSetList);
        return PreferencesService.getPreference(PreferenceConstants.LOADOUTS_PREFIX + fid, "N/A");
    }

    public void selectLoadoutSet(final String activeLoadoutSetUUID, final String fid) {
        final LoadoutSetList loadoutSetList = getLoadoutSetList(fid);
        loadoutSetList.setSelectedLoadoutSetUUID(activeLoadoutSetUUID);
        saveLoadoutSetList(fid, loadoutSetList);
    }

    public void deleteLoadoutSet(final String activeLoadoutSetUUID, final String fid) {
        final LoadoutSetList loadoutSetList = getLoadoutSetList(fid);
        loadoutSetList.delete(activeLoadoutSetUUID);
        saveLoadoutSetList(fid, loadoutSetList);
    }

    public void saveLoadoutSetList(final String fid, final LoadoutSetList loadoutSetList) {
        try {
            PreferencesService.setPreference(PreferenceConstants.LOADOUTS_PREFIX + fid, OBJECT_MAPPER.writeValueAsString(loadoutSetList));
        } catch (final JsonProcessingException e) {
            log.error("Failed to save loadouts", e);
        }
    }

    public void saveLoadoutSet(final String fid, final LoadoutSet loadoutSet) {
        final LoadoutSetList loadoutSetList = getLoadoutSetList(fid);
        loadoutSetList.updateLoadoutSet(loadoutSet);
        saveLoadoutSetList(fid, loadoutSetList);
    }

}
