// Copyright (c) 2026 Gridmark Technologies Ltd (HushChip)
// https://github.com/hushchip/HushChip-Android
//
// Based on Seedkeeper-Android by Toporin / Satochip S.R.L.
// https://github.com/Toporin/Seedkeeper-Android
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

package uk.co.hushchip.app.data

enum class NfcActionType {
    DO_NOTHING,
    SCAN_CARD,
    SETUP_CARD,
    SETUP_CARD_FOR_BACKUP,
    CHANGE_PIN,
    EXPORT_SECRET,
    DELETE_SECRET,
    IMPORT_SECRET,
    EDIT_CARD_LABEL,
    CARD_LOGS,
    SCAN_BACKUP_CARD,
    EXPORT_SECRETS_FROM_MASTER,
    IMPORT_SECRETS_TO_BACKUP,
    RESET_CARD,
}