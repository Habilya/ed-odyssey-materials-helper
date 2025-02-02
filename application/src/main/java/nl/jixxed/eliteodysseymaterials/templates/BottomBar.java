package nl.jixxed.eliteodysseymaterials.templates;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import jfxtras.styles.jmetro.JMetroStyleClass;
import nl.jixxed.eliteodysseymaterials.builder.ComboBoxBuilder;
import nl.jixxed.eliteodysseymaterials.builder.LabelBuilder;
import nl.jixxed.eliteodysseymaterials.constants.AppConstants;
import nl.jixxed.eliteodysseymaterials.constants.OsConstants;
import nl.jixxed.eliteodysseymaterials.constants.PreferenceConstants;
import nl.jixxed.eliteodysseymaterials.domain.ApplicationState;
import nl.jixxed.eliteodysseymaterials.domain.Commander;
import nl.jixxed.eliteodysseymaterials.enums.FontSize;
import nl.jixxed.eliteodysseymaterials.helper.POIHelper;
import nl.jixxed.eliteodysseymaterials.service.CAPIService;
import nl.jixxed.eliteodysseymaterials.service.LocaleService;
import nl.jixxed.eliteodysseymaterials.service.PreferencesService;
import nl.jixxed.eliteodysseymaterials.service.event.*;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

class BottomBar extends HBox {

    private static final ApplicationState APPLICATION_STATE = ApplicationState.getInstance();

    private String system = "";
    private String body = "";
    private String station = "";

    private Label gameModeLabel;
    private Label apiLabel;
    private Label watchedFileLabel;
    private Label login;
    private Label commanderLabel;
    private Label locationLabel;
    private Region region;
    private ComboBox<Commander> commanderSelect;
    private Double latitude;
    private Double longitude;
    private Separator apiLabelSeparator;

    BottomBar() {
        initComponents();
        initEventHandling();
    }

    private void initComponents() {
        this.getStyleClass().add("bottombar");
        this.getStyleClass().add(JMetroStyleClass.BACKGROUND);
        this.region = new Region();
        HBox.setHgrow(this.region, Priority.ALWAYS);
        this.locationLabel = LabelBuilder.builder().build();
        this.apiLabel = LabelBuilder.builder().build();
        this.gameModeLabel = LabelBuilder.builder().build();
        this.commanderLabel = LabelBuilder.builder().withText(LocaleService.getStringBinding("tab.settings.commander")).build();
        this.login = LabelBuilder.builder().withText(LocaleService.getStringBinding("statusbar.login")).build();
        this.commanderSelect = ComboBoxBuilder.builder(Commander.class)
                .withStyleClass("bottombar-dropdown")
                .withItemsProperty(FXCollections.observableArrayList(APPLICATION_STATE.getCommanders()))
                .withValueChangeListener((obs, oldValue, newValue) -> Platform.runLater(() -> {
                    if (newValue != null) {
                        PreferencesService.setPreference(PreferenceConstants.COMMANDER, newValue);
                    }
                    if (oldValue != null && newValue != null) {
                        EventService.publish(new CommanderSelectedEvent(newValue));
                    }
                }))
                .build();
        this.commanderSelect.styleProperty().set("-fx-font-size: " + FontSize.valueOf(PreferencesService.getPreference(PreferenceConstants.TEXTSIZE, "NORMAL")).getSize() + "px");

        final File watchedFolder = new File(PreferencesService.getPreference(PreferenceConstants.JOURNAL_FOLDER, OsConstants.DEFAULT_WATCHED_FOLDER));
        this.watchedFileLabel = LabelBuilder.builder().withText(LocaleService.getStringBinding("statusbar.watching.none", watchedFolder.getAbsolutePath())).build();
        this.apiLabelSeparator = new Separator(Orientation.VERTICAL);
        this.apiLabelSeparator.visibleProperty().bind(CAPIService.getInstance().getActive());
        this.apiLabel.visibleProperty().bind(CAPIService.getInstance().getActive());
        this.getChildren().addAll(this.watchedFileLabel, new Separator(Orientation.VERTICAL), this.gameModeLabel, this.apiLabelSeparator, this.apiLabel, this.login, this.region, this.locationLabel, new Separator(Orientation.VERTICAL), this.commanderLabel, this.commanderSelect);
    }

    private void initEventHandling() {
        EventService.addListener(this, 0, WatchedFolderChangedEvent.class, this::resetAfterWatchedFolderChanged);
        EventService.addListener(this, LocationChangedEvent.class, this::updateLocationLabel);
        EventService.addListener(this, JournalLineProcessedEvent.class, this::updateWatchedFileLabel);
        EventService.addListener(this, EngineerEvent.class, event -> hideLoginRequest());
        EventService.addListener(this, CommanderAddedEvent.class, this::handleAddedCommander);
        EventService.addListener(this, 0, CommanderAllListedEvent.class, event -> afterAllCommandersListed());
        EventService.addListener(this, 0, CommanderResetEvent.class, event -> this.commanderSelect.getItems().clear());
        EventService.addListener(this, AfterFontSizeSetEvent.class, fontSizeEvent -> this.commanderSelect.styleProperty().set("-fx-font-size: " + fontSizeEvent.getFontSize() + "px"));
        EventService.addListener(this, LoadGameEvent.class, this::handleLoadGame);
        EventService.addListener(this, JournalInitEvent.class, event -> updateApiLabel());
        EventService.addListener(this, CapiFleetCarrierEvent.class, event -> updateApiLabel());
    }

    private void afterAllCommandersListed() {
        if (!this.commanderSelect.getItems().isEmpty() && this.commanderSelect.getSelectionModel().getSelectedIndex() == -1) {
            this.commanderSelect.getSelectionModel().select(this.commanderSelect.getItems().get(0));
            PreferencesService.setPreference(PreferenceConstants.COMMANDER, this.commanderSelect.getItems().get(0));
            EventService.publish(new CommanderSelectedEvent(this.commanderSelect.getItems().get(0)));
        }
    }

    private void handleAddedCommander(final CommanderAddedEvent commanderAddedEvent) {
        this.login.setVisible(true);
        this.login.getStyleClass().remove("statusbar-login-hidden");
        this.commanderSelect.getItems().add(commanderAddedEvent.getCommander());
        final String preferredName = PreferencesService.getPreference(PreferenceConstants.COMMANDER, "");
        if (preferredName.isBlank() || commanderAddedEvent.getCommander().getName().equals(preferredName)) {
            this.commanderSelect.getSelectionModel().select(commanderAddedEvent.getCommander());
        }
    }

    private void handleLoadGame(final LoadGameEvent loadGameEvent) {
        this.gameModeLabel.textProperty().bind(LocaleService.getStringBinding(loadGameEvent.getExpansion().getLocalizationKey()));
    }

    private void hideLoginRequest() {
        this.login.setVisible(false);
        if (!this.login.getStyleClass().contains("statusbar-login-hidden")) {
            this.login.getStyleClass().add("statusbar-login-hidden");
        }
    }

    private void resetAfterWatchedFolderChanged(final WatchedFolderChangedEvent watchedFolderChangedEvent) {
        this.commanderSelect.getItems().clear();
        this.watchedFileLabel.textProperty().bind(LocaleService.getStringBinding("statusbar.watching.none", watchedFolderChangedEvent.getPath()));
    }

    private void updateWatchedFileLabel(final JournalLineProcessedEvent journalLineProcessedEvent) {
        if (journalLineProcessedEvent.getFile().getName().endsWith("log")) {
            Platform.runLater(() -> this.watchedFileLabel.textProperty().bind(LocaleService.getStringBinding("statusbar.watching", journalLineProcessedEvent.getFile().getName())));
        }
    }

    private void updateApiLabel() {

        APPLICATION_STATE.getPreferredCommander().ifPresent(commander -> {
            final String pathname = OsConstants.CONFIG_DIRECTORY + OsConstants.OS_SLASH + commander.getFid().toLowerCase(Locale.ENGLISH);
            final File fleetCarrierFileDir = new File(pathname);
            fleetCarrierFileDir.mkdirs();
            final File fleetCarrierFile = new File(pathname + OsConstants.OS_SLASH + AppConstants.FLEETCARRIER_FILE);
            if (fleetCarrierFile.exists()) {
                final ZonedDateTime lastModified = ZonedDateTime.ofInstant(Instant.ofEpochMilli(fleetCarrierFile.lastModified()), ZoneId.systemDefault());
                this.apiLabel.textProperty().bind(LocaleService.getStringBinding("statusbar.api.last.update", lastModified.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))));
            }
        });
    }

    private void updateLocationLabel(final LocationChangedEvent locationChangedEvent) {
        this.system = locationChangedEvent.getCurrentStarSystem().getName();
        this.body = locationChangedEvent.getCurrentBody();
        this.station = locationChangedEvent.getCurrentSettlement();
        this.latitude = locationChangedEvent.getCurrentLatitude();
        this.longitude = locationChangedEvent.getCurrentLongitude();
        Platform.runLater(() -> this.locationLabel.setText(this.system +
                (this.body.isBlank() ? "" : " | " + this.body) +
                (this.station.isBlank() || this.station.equals(this.body) ? "" : " | " + POIHelper.map(this.station)) +
                (this.latitude != null && !this.latitude.equals(999.9) ? " (" + this.latitude + ", " + this.longitude + ")" : "")
        ));
    }
}
