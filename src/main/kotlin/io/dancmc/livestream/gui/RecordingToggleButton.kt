package io.dancmc.livestream.gui

import javafx.event.ActionEvent
import javafx.scene.control.ToggleButton

class RecordingToggleButton(text:String="") :ToggleButton(text){

    override fun fire() {
        if (!isDisabled) {
            if(!isSelected) isSelected = true
            fireEvent(ActionEvent())
        }
    }
}