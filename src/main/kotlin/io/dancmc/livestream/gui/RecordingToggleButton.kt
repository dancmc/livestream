package io.dancmc.livestream.gui

import javafx.event.ActionEvent
import javafx.scene.control.ToggleButton

/**
 * Minor customisation to toggle button to prevent it from being actively unselected
 */
class RecordingToggleButton(text:String="") :ToggleButton(text){

    override fun fire() {
        if (!isDisabled) {
            if(!isSelected) isSelected = true
            fireEvent(ActionEvent())
        }
    }
}