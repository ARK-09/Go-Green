package com.arkindustries.gogreen.socket.request

import com.arkindustries.gogreen.api.response.AttachmentResponse

data class MessageRequest (val roomId: String, val message: String, val attachments: AttachmentResponse?)