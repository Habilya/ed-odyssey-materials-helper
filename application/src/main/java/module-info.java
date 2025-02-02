module nl.jixxed.eliteodysseymaterials {
    requires jdk.crypto.ec;
    requires javafx.fxml;
    requires transitive javafx.controls;
    requires transitive javafx.media;
    requires javafx.swing;
    requires org.controlsfx.controls;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires io.reactivex.rxjava3;
    requires java.net.http;
    requires org.jfxtras.styles.jmetro;
    requires org.slf4j;
    requires com.google.common;
    requires java.naming;
    requires jdk.naming.dns;
    requires static lombok;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires org.apache.poi.ooxml.schemas;
    requires org.apache.commons.csv;
    requires scribejava.core;
    requires org.openpnp;
    requires net.java.dev.jna;
    requires com.sun.jna.platform;
    requires nl.jixxed.tess4j;
    requires nl.jixxed.lept4j;
    requires org.apache.commons.io;
    requires java.desktop;
    opens nl.jixxed.eliteodysseymaterials to javafx.graphics, java.desktop;
    opens nl.jixxed.eliteodysseymaterials.service.message to com.fasterxml.jackson.databind;
    opens nl.jixxed.eliteodysseymaterials.trade.message.outbound to com.fasterxml.jackson.databind;
    opens nl.jixxed.eliteodysseymaterials.trade.message.outbound.payload to com.fasterxml.jackson.databind;
    opens nl.jixxed.eliteodysseymaterials.trade.message.common to com.fasterxml.jackson.databind;
    opens nl.jixxed.eliteodysseymaterials.trade.message.inbound to com.fasterxml.jackson.databind;
    opens nl.jixxed.eliteodysseymaterials.templates to javafx.fxml, org.controlsfx.controls;
    opens nl.jixxed.eliteodysseymaterials.templates.destroyables to javafx.fxml, org.controlsfx.controls;
    exports nl.jixxed.eliteodysseymaterials;
    exports nl.jixxed.eliteodysseymaterials.enums;
    exports nl.jixxed.eliteodysseymaterials.domain;
    exports nl.jixxed.eliteodysseymaterials.templates;
    exports nl.jixxed.eliteodysseymaterials.service;
    opens nl.jixxed.eliteodysseymaterials.domain to com.fasterxml.jackson.databind;
    opens nl.jixxed.eliteodysseymaterials.enums to com.fasterxml.jackson.databind;
    opens nl.jixxed.eliteodysseymaterials.templates.components.segmentbar to javafx.fxml, org.controlsfx.controls;
    exports nl.jixxed.eliteodysseymaterials.service.ar;
    exports nl.jixxed.eliteodysseymaterials.templates.horizons;
    opens nl.jixxed.eliteodysseymaterials.templates.horizons to javafx.fxml, org.controlsfx.controls;
    exports nl.jixxed.eliteodysseymaterials.templates.odyssey;
    opens nl.jixxed.eliteodysseymaterials.templates.odyssey to javafx.fxml, org.controlsfx.controls;
    exports nl.jixxed.eliteodysseymaterials.templates.horizons.commodities;
    opens nl.jixxed.eliteodysseymaterials.templates.horizons.commodities to javafx.fxml, org.controlsfx.controls;
    exports nl.jixxed.eliteodysseymaterials.templates.horizons.engineers;
    opens nl.jixxed.eliteodysseymaterials.templates.horizons.engineers to javafx.fxml, org.controlsfx.controls;
    exports nl.jixxed.eliteodysseymaterials.templates.horizons.materials;
    opens nl.jixxed.eliteodysseymaterials.templates.horizons.materials to javafx.fxml, org.controlsfx.controls;
    exports nl.jixxed.eliteodysseymaterials.templates.horizons.wishlist;
    opens nl.jixxed.eliteodysseymaterials.templates.horizons.wishlist to javafx.fxml, org.controlsfx.controls;
    exports nl.jixxed.eliteodysseymaterials.templates.odyssey.materials;
    opens nl.jixxed.eliteodysseymaterials.templates.odyssey.materials to javafx.fxml, org.controlsfx.controls;
    exports nl.jixxed.eliteodysseymaterials.templates.odyssey.loadout;
    opens nl.jixxed.eliteodysseymaterials.templates.odyssey.loadout to javafx.fxml, org.controlsfx.controls;
    exports nl.jixxed.eliteodysseymaterials.templates.odyssey.engineers;
    opens nl.jixxed.eliteodysseymaterials.templates.odyssey.engineers to javafx.fxml, org.controlsfx.controls;
    exports nl.jixxed.eliteodysseymaterials.templates.odyssey.wishlist;
    opens nl.jixxed.eliteodysseymaterials.templates.odyssey.wishlist to javafx.fxml, org.controlsfx.controls;
    exports nl.jixxed.eliteodysseymaterials.templates.odyssey.trade;
    opens nl.jixxed.eliteodysseymaterials.templates.odyssey.trade to javafx.fxml, org.controlsfx.controls;
    exports nl.jixxed.eliteodysseymaterials.templates.components;
    opens nl.jixxed.eliteodysseymaterials.templates.components to javafx.fxml, org.controlsfx.controls;
    exports nl.jixxed.eliteodysseymaterials.templates.overlay.ar;
    opens nl.jixxed.eliteodysseymaterials.templates.overlay.ar to javafx.fxml, org.controlsfx.controls;
    exports nl.jixxed.eliteodysseymaterials.templates.generic;
    opens nl.jixxed.eliteodysseymaterials.templates.generic to javafx.fxml, org.controlsfx.controls;
    exports nl.jixxed.eliteodysseymaterials.templates.settings;
    opens nl.jixxed.eliteodysseymaterials.templates.settings to javafx.fxml, org.controlsfx.controls;
    exports nl.jixxed.eliteodysseymaterials.templates.dialog;
    opens nl.jixxed.eliteodysseymaterials.templates.dialog to javafx.fxml, org.controlsfx.controls;
}