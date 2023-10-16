package com.arkindustries.gogreen.socket.request

import com.arkindustries.gogreen.api.request.AttachmentRequest

data class Message (val roomId: String, val message: String, val attachments: AttachmentRequest)