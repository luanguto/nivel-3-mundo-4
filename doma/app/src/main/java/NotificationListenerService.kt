package com.example.doma

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Aqui você pode processar notificações
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Aqui você pode processar a remoção de notificações
    }
}
