package vn.techres.line.helper.techresenum

enum class TechResEnumChat {
    ACTION_REPLY_MESSAGE {
        override fun toString(): String {
            return "ACTION_REPLY_MESSAGE"
        }
    },
    EXTRA_REPLY_MESSAGE {
        override fun toString(): String {
            return "EXTRA_REPLY_MESSAGE"
        }
    },
    GROUP_ID {
        override fun toString(): String {
            return "GROUP_ID"
        }
    },
    PAGE_GROUP {
        override fun toString(): String {
            return "PAGE_GROUP"
        }
    },
    SMOOTH_SCROLL {
        override fun toString(): String {
            return "SMOOTH_SCROLL"
        }
    },
    SAVE_GROUP {
        override fun toString(): String {
            return "SAVE_GROUP"
        }
    },
    SAVE_FRIEND_ONLINE {
        override fun toString(): String {
            return "SAVE_FRIEND_ONLINE"
        }
    },
    CONVERSATION_TYPE {
        override fun toString(): String {
            return "CONVERSATION_TYPE"
        }
    },
    RES_REMOVE_GROUP_ALO_LINE {
        override fun toString(): String {
            return "res-remove-group-aloline"
        }
    },
    REMOVE_GROUP_ALO_LINE {
        override fun toString(): String {
            return "remove-group-aloline"
        }
    },

    /**
     * Type file upload
     * */
    TYPE_GROUP_FILE {
        override fun toString(): String {
            return "3"
        }
    },
    TYPE_PERSONAL_FILE {
        override fun toString(): String {
            return "4"
        }
    },

    /**
     * Type file message
     * */
    TYPE_DOC {
        override fun toString(): String {
            return "doc"
        }
    },
    TYPE_DOCX {
        override fun toString(): String {
            return "docx"
        }
    },
    TYPE_JPG {
        override fun toString(): String {
            return "jpg"
        }
    },
    TYPE_SVG {
        override fun toString(): String {
            return "svg"
        }
    },
    TYPE_JPEG {
        override fun toString(): String {
            return "jpeg"
        }
    },
    TYPE_PNG {
        override fun toString(): String {
            return "png"
        }
    },
    TYPE_AI {
        override fun toString(): String {
            return "ai"
        }
    },
    TYPE_DMG {
        override fun toString(): String {
            return "dmg"
        }
    },
    TYPE_XLSX {
        override fun toString(): String {
            return "xlsx"
        }
    },
    TYPE_HTML {
        override fun toString(): String {
            return "html"
        }
    },
    TYPE_MP3 {
        override fun toString(): String {
            return "mp3"
        }
    },
    TYPE_PDF {
        override fun toString(): String {
            return "pdf"
        }
    },
    TYPE_PPTX {
        override fun toString(): String {
            return "pptx"
        }
    },
    TYPE_PSD {
        override fun toString(): String {
            return "psd"
        }
    },
    TYPE_REC {
        override fun toString(): String {
            return "rec"
        }
    },
    TYPE_EXE {
        override fun toString(): String {
            return "exe"
        }
    },
    TYPE_SKETCH {
        override fun toString(): String {
            return "sketch"
        }
    },
    TYPE_TXT {
        override fun toString(): String {
            return "txt"
        }
    },
    TYPE_MP4 {
        override fun toString(): String {
            return "mp4"
        }
    },
    TYPE_XML {
        override fun toString(): String {
            return "xml"
        }
    },
    TYPE_ZIP {
        override fun toString(): String {
            return "zip"
        }
    },
    TYPE_RAR {
        override fun toString(): String {
            return "rar"
        }
    },
    TYPE_SMMX {
        override fun toString(): String {
            return "smmx"
        }
    },

    /**
     * Type message
     * */
    TYPE_CREATE_GROUP {
        override fun toString(): String {
            return "create_group"
        }
    },
    TYPE_CREATE_GROUP_PERSONAL {
        override fun toString(): String {
            return "create_group_personal"
        }
    },
    TYPE_FILE {
        override fun toString(): String {
            return "file"
        }
    },
    TYPE_STICKER {
        override fun toString(): String {
            return "sticker"
        }
    },
    TYPE_IMAGE {
        override fun toString(): String {
            return "image"
        }
    },
    TYPE_TEXT {
        override fun toString(): String {
            return "text"
        }
    },
    TYPE_VIDEO {
        override fun toString(): String {
            return "video"
        }
    },
    TYPE_REPLY {
        override fun toString(): String {
            return "reply"
        }
    },
    TYPE_AUDIO {
        override fun toString(): String {
            return "audio"
        }
    },
    TYPE_REVOKE {
        override fun toString(): String {
            return "revoke_message"
        }
    },
    TYPE_BACKGROUND {
        override fun toString(): String {
            return "background"
        }
    },
    TYPE_MEMBER_LEAVE_GROUP {
        override fun toString(): String {
            return "member_leave_group"
        }
    },
    TYPE_KICK_MEMBER {
        override fun toString(): String {
            return "kick_member"
        }
    },
    TYPE_AVATAR {
        override fun toString(): String {
            return "avatar"
        }
    },
    TYPE_BUSINESS_CARD {
        override fun toString(): String {
            return "business_card"
        }
    },
    TYPE_COMPLETE_BILL {
        override fun toString(): String {
            return "complete_bill"
        }
    },
    TYPE_GETTINGS {
        override fun toString(): String {
            return "gettings"
        }
    },
    TYPE_GIFT_NOTIFICATION {
        override fun toString(): String {
            return "gift"
        }
    },
    TYPE_AUTHORIZE {
        override fun toString(): String {
            return "authorize"
        }
    },
    TYPE_GROUP_NAME {
        override fun toString(): String {
            return "group_name"
        }
    },
    TYPE_PINNED {
        override fun toString(): String {
            return "pinned"
        }
    },
    TYPE_ADD_NEW_USER {
        override fun toString(): String {
            return "add_new_user"
        }
    },
    TYPE_TYPING {
        override fun toString(): String {
            return "typing"
        }
    },
    TYPE_LOAD_PAGE {
        override fun toString(): String {
            return "load_page"
        }
    },
    TYPE_LINK {
        override fun toString(): String {
            return "link"
        }
    },
    TYPE_REMOVE_PINNED {
        override fun toString(): String {
            return "remove_pinned"
        }
    },
    TYPE_LEAVE_GROUP {
        override fun toString(): String {
            return "member_leave_group"
        }
    },
    TYPE_SHARE {
        override fun toString(): String {
            return "share"
        }
    },
    TYPE_GI_PHY {
        override fun toString(): String {
            return "giphy"
        }
    },
    TYPE_VOTE {
        override fun toString(): String {
            return "vote"
        }
    },
    TYPE_PROMOTE_VICE {
        override fun toString(): String {
            return "promote_vice"
        }
    },
    TYPE_DEMOTED_VICE {
        override fun toString(): String {
            return "demoted_vice"
        }
    },
    TYPE_ADVERTISEMENT {
        override fun toString(): String {
            return "ads"
        }
    },
    TYPE_NOTIFICATION_BIRTHDAY {
        override fun toString(): String {
            return "notify_birthday"
        }
    },
    TYPE_INVITE_EVENT {
        override fun toString(): String {
            return "invite_event"
        }
    },
    TYPE_MESSAGE_INFORMATION {
        override fun toString(): String {
            return "information"
        }
    },

    /**
     * Share Message
     * */
    CHAT_SHARE_ALO_LINE {
        override fun toString(): String {
            return "chat-share-aloline"
        }
    },
    RES_CHAT_SHARE_ALO_LINE {
        override fun toString(): String {
            return "res-chat-share-aloline"
        }
    },
    RES_CHAT_SHARE_NOTE_ALO_LINE {
        override fun toString(): String {
            return "res-chat-share-note-aloline"
        }
    },

    /**
     * Socket Group Chat
     * --------------------------------------------------------------------------------------------
     * */
    JOIN_ROOM_GROUP_ALO_LINE {
        override fun toString(): String {
            return "join-room-aloline"
        }
    },
    LEAVE_ROOM_GROUP_ALO_LINE {
        override fun toString(): String {
            return "leave-room-aloline"
        }
    },
    MESSAGE_NOT_SEEN_BY_ONE_GROUP {
        override fun toString(): String {
            return "message-not-seen-by-one-group"
        }
    },

    /**
     * Promoted group vice
     * */
    PROMOTE_GROUP_VICE_ALO_LINE {
        override fun toString(): String {
            return "promote-group-vice-aloline"
        }
    },
    RES_PROMOTE_GROUP_VICE_ALO_LINE {
        override fun toString(): String {
            return "res-promote-group-vice-aloline"
        }
    },

    /**
     * Demoted group vice
     * */
    DEMOTED_GROUP_VICE_ALO_LINE {
        override fun toString(): String {
            return "demoted-group-vice-aloline"
        }
    },
    RES_DEMOTED_GROUP_VICE_ALO_LINE {
        override fun toString(): String {
            return "res-demoted-group-vice-aloline"
        }
    },

    /**
     * Reply
     * */
    CHAT_REPLY_ALO_LINE {
        override fun toString(): String {
            return "chat-reply-aloline"
        }
    },
    RES_CHAT_REPLY_ALO_LINE {
        override fun toString(): String {
            return "res-chat-reply-aloline"
        }
    },

    /**
     * Link
     * */
    CHAT_LINK_ALO_LINE {
        override fun toString(): String {
            return "chat-link-aloline"
        }
    },
    RES_CHAT_LINK_ALO_LINE {
        override fun toString(): String {
            return "res-chat-link-aloline"
        }
    },

    /**
     * Create Group
     * */
    NEW_GROUP_CREATE_ALO_LINE {
        override fun toString(): String {
            return "new-group-created-aloline"
        }
    },
    RES_NEW_GROUP_CREATE_ALO_LINE {
        override fun toString(): String {
            return "res-new-group-created-aloline"
        }
    },

    /**
     * Typing on
     * */
    TYPING_ON_GROUP_ALO_LINE {
        override fun toString(): String {
            return "user-is-typing-aloline"
        }
    },
    RES_TYPING_ON_GROUP_ALO_LINE {
        override fun toString(): String {
            return "res-user-is-typing-aloline"
        }
    },

    /**
     * Typing off
     * */
    TYPING_OFF_GROUP_ALO_LINE {
        override fun toString(): String {
            return "user-is-not-typing-aloline"
        }
    },
    RES_TYPING_OFF_GROUP_ALO_LINE {
        override fun toString(): String {
            return "res-user-is-not-typing-aloline"
        }
    },

    /**
     * Pinned
     * */
    PINNED_MESSAGE_ALO_LINE {
        override fun toString(): String {
            return "pinned-message-aloline"
        }
    },
    RES_PINNED_MESSAGE_ALO_LINE {
        override fun toString(): String {
            return "res-pinned-message-aloline"
        }
    },
    RES_PINNED_MESSAGE_TEXT_ALO_LINE {
        override fun toString(): String {
            return "res-pinned-message-text-aloline"
        }
    },

    /**
     * Revoke Pinned
     * */
    REVOKE_PINNED_MESSAGE_ALO_LINE {
        override fun toString(): String {
            return "revoke-pinned-message-aloline"
        }
    },
    RES_REVOKE_PINNED_MESSAGE_ALO_LINE {
        override fun toString(): String {
            return "res-revoke-pinned-message-aloline"
        }
    },
    RES_REVOKE_PINNED_MESSAGE_TEXT_ALO_LINE {
        override fun toString(): String {
            return "res-revoke-pinned-message-text-aloline"
        }
    },

    /**
     * Update name
     * */
    UPDATE_GROUP_NAME_ALO_LINE {
        override fun toString(): String {
            return "update-group-name-aloline"
        }
    },
    RES_UPDATE_GROUP_NAME_ALO_LINE {
        override fun toString(): String {
            return "res-update-group-name-aloline"
        }
    },

    /**
     * Update Avatar
     * */
    UPDATE_GROUP_AVATAR_ALO_LINE {
        override fun toString(): String {
            return "update-group-avatar-aloline"
        }
    },
    RES_UPDATE_GROUP_AVATAR_ALO_LINE {
        override fun toString(): String {
            return "res-update-group-avatar-aloline"
        }
    },

    /**
     * Update Background
     * */
    UPDATE_GROUP_BACKGROUND_ALO_LINE {
        override fun toString(): String {
            return "update-group-background-aloline"
        }
    },
    RES_UPDATE_GROUP_BACKGROUND_ALO_LINE {
        override fun toString(): String {
            return "res-update-group-background-aloline"
        }
    },

    /**
     * Add Member
     * */
    ADD_NEW_USER_ALO_LINE {
        override fun toString(): String {
            return "add-new-user-aloline"
        }
    },
    RES_ADD_NEW_USER_ALO_LINE {
        override fun toString(): String {
            return "res-add-new-user-aloline"
        }
    },

    /**
     * Remove Member
     * */
    REMOVE_USER_ALO_LINE {
        override fun toString(): String {
            return "remove-user-aloline"
        }
    },
    RES_REMOVE_USER_ALO_LINE {
        override fun toString(): String {
            return "res-remove-user-aloline"
        }
    },

    /**
     * Authorize
     * */
    ADMIN_AUTHORIZE_MEMBER_ALO_LINE {
        override fun toString(): String {
            return "admin-authorize-member-aloline"
        }
    },
    RES_ADMIN_AUTHORIZE_MEMBER_ALO_LINE {
        override fun toString(): String {
            return "res-admin-authorize-member-aloline"
        }
    },

    /**
     * Text
     * */
    CHAT_TEXT_ALO_LINE {
        override fun toString(): String {
            return "chat-text-aloline"
        }
    },
    RES_CHAT_TEXT_ALO_LINE {
        override fun toString(): String {
            return "res-chat-text-aloline"
        }
    },

    /**
     * Sticker
     * */
    CHAT_STICKER_ALO_LINE {
        override fun toString(): String {
            return "chat-sticker-aloline"
        }
    },
    RES_CHAT_STICKER_ALO_LINE {
        override fun toString(): String {
            return "res-chat-sticker-aloline"
        }
    },

    /**
     * Business Card
     * */
    BUSINESS_CARD_ALO_LINE {
        override fun toString(): String {
            return "business-card-aloline"
        }
    },
    RES_BUSINESS_CARD_ALO_LINE {
        override fun toString(): String {
            return "res-business-card-aloline"
        }
    },

    /**
     * Vote
     * */
    CREATE_VOTE_ALO_LINE {
        override fun toString(): String {
            return "create_vote-aloline"
        }
    },
    RES_CREATE_VOTE_ALO_LINE {
        override fun toString(): String {
            return "res-create_vote-aloline"
        }
    },

    /**
     * Image
     * */
    CHAT_IMAGE_ALO_LINE {
        override fun toString(): String {
            return "chat-image-aloline"
        }
    },
    RES_CHAT_IMAGE_ALO_LINE {
        override fun toString(): String {
            return "res-chat-image-aloline"
        }
    },

    /**
     * Video
     * */
    CHAT_VIDEO_ALO_LINE {
        override fun toString(): String {
            return "chat-video-aloline"
        }
    },
    RES_CHAT_VIDEO_ALO_LINE {
        override fun toString(): String {
            return "res-chat-video-aloline"
        }
    },

    /**
     * Audio
     * */
    CHAT_AUDIO_ALO_LINE {
        override fun toString(): String {
            return "chat-audio-aloline"
        }
    },

    RES_CHAT_AUDIO_ALO_LINE {
        override fun toString(): String {
            return "res-chat-audio-aloline"
        }
    },

    /**
     * Reaction
     * */
    REACTION_MESSAGE_ALO_LINE {
        override fun toString(): String {
            return "reaction-message-aloline"
        }
    },
    RES_REACTION_MESSAGE_ALO_LINE {
        override fun toString(): String {
            return "res-reaction-message-aloline"
        }
    },

    /**
     * Revoke
     * */
    REVOKE_MESSAGE_ALO_LINE {
        override fun toString(): String {
            return "revoke-message-aloline"
        }
    },
    REVOKE_REACTION_MESSAGE_ALO_LINE {
        override fun toString(): String {
            return "revoke-reaction-message-aloline"
        }
    },
    RES_REVOKE_MESSAGE_ALO_LINE {
        override fun toString(): String {
            return "res-revoke-message-aloline"
        }
    },
    RES_REVOKE_MESSAGE_REPLY_ALO_LINE {
        override fun toString(): String {
            return "res-revoke-message-reply-aloline"
        }
    },

    /**
     * Member Leave
     * */
    MEMBER_LEAVE_GROUP_ALO_LINE {
        override fun toString(): String {
            return "member-leave-group-aloline"
        }
    },
    RES_MEMBER_LEAVE_GROUP_ALO_LINE {
        override fun toString(): String {
            return "res-member-leave-group-aloline"
        }
    },

    /**
     * File
     * */
    CHAT_FILE_ALO_LINE {
        override fun toString(): String {
            return "chat-file-aloline"
        }
    },
    RES_CHAT_FILE_ALO_LINE {
        override fun toString(): String {
            return "res-chat-file-aloline"
        }
    },

    /**
     * Message Viewed
     * */
    RES_MESSAGE_VIEWED_ALO_LINE {
        override fun toString(): String {
            return "res-message-viewed-aloline"
        }
    },
    RES_LIST_MESSAGE_VIEWED_ALO_LINE {
        override fun toString(): String {
            return "res-list-message-viewed-aloline"
        }
    },

    /**
     * Socket Personal Chat
     * --------------------------------------------------------------------------------------------
     * */
    JOIN_ROOM_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "join-room-aloline-personal"
        }
    },
    LEAVE_ROOM_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "leave-room-aloline-personal"
        }
    },
    MESSAGE_NOT_SEEN_BY_ONE_GROUP_PERSONAL {
        override fun toString(): String {
            return "message-not-seen-by-one-group-personal"
        }
    },

    /**
     * File
     * */
    CHAT_FILE_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "chat-file-personal-aloline"
        }
    },
    RES_CHAT_FILE_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "res-chat-file-personal-aloline"
        }
    },

    /**
     * new group personal
     * */
    NEW_GROUP_CREATE_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "new-group-created-aloline-personal"
        }
    },
    RES_NEW_GROUP_CREATE_ALO_LINE_PERSONAL {
        override fun toString(): String {
            return "res-new-group-created-aloline-personal"
        }
    },

    /**
     * Typing on
     * */
    TYPING_ON_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "user-is-typing-personal-aloline"
        }
    },
    RES_TYPING_ON_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "res-user-is-typing-personal-aloline"
        }
    },

    /**
     * Typing off
     * */
    TYPING_OFF_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "user-is-not-typing-personal-aloline"
        }
    },
    RES_TYPING_OFF_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "res-user-is-not-typing-personal-aloline"
        }
    },

    /**
     * chat text personal
     * */
    CHAT_TEXT_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "chat-text-emoji-personal-aloline"
        }
    },
    RES_CHAT_TEXT_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "res-chat-text-emoji-personal-aloline"
        }
    },

    /**
     * chat image personal
     * */
    CHAT_PERSONAL_IMAGE_ALO_LINE {
        override fun toString(): String {
            return "chat-image-personal-aloline"
        }
    },
    RES_CHAT_PERSONAL_IMAGE_ALO_LINE {
        override fun toString(): String {
            return "res-chat-image-personal-aloline"
        }
    },

    /**
     * chat video personal
     * */
    CHAT_PERSONAL_VIDEO_ALO_LINE {
        override fun toString(): String {
            return "chat-video-personal-aloline"
        }
    },
    RES_CHAT_PERSONAL_VIDEO_ALO_LINE {
        override fun toString(): String {
            return "res-chat-video-personal-aloline"
        }
    },

    /**
     * chat audio personal
     * */
    CHAT_AUDIO_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "chat-audio-personal-aloline"
        }
    },
    RES_CHAT_AUDIO_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "res-chat-audio-personal-aloline"
        }
    },

    /**
     * chat link personal
     * */
    CHAT_LINK_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "chat-link-personal-aloline"
        }
    },
    RES_CHAT_LINK_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "res-chat-link-personal-aloline"
        }
    },

    /**
     * reaction personal
     * */
    REACTION_MESSAGE_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "reaction-message-personal-aloline"
        }
    },
    RES_MESSAGE_PERSONAL_REACTION_ALO_LINE {
        override fun toString(): String {
            return "res-reaction-message-personal-aloline"
        }
    },

    /**
     * pinned personal
     * */
    PINNED_MESSAGE_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "pinned-message-personal-aloline"
        }
    },
    RES_PINNED_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "res-pinned-message-personal-aloline"
        }
    },
    RES_PINNED_MESSAGE_TEXT_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "res-pinned-message-text-personal-aloline"
        }
    },
    REVOKE_PINNED_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "revoke-pinned-message-personal-aloline"
        }
    },
    RES_REVOKE_PINNED_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "res-revoke-pinned-message-personal-aloline"
        }
    },
    RES_REVOKE_PINNED_MESSAGE_TEXT_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "res-revoke-pinned-message-text-personal-aloline"
        }
    },

    /**
     * revoke personal
     * */
    REVOKE_MESSAGE_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "revoke-message-personal-aloline"
        }
    },
    RES_REVOKE_MESSAGE_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "res-revoke-message-personal-aloline"
        }
    },

    /**
     * reply personal
     * */
    CHAT_REPLY_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "chat-reply-personal-aloline"
        }
    },
    RES_CHAT_REPLY_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "res-chat-reply-personal-aloline"
        }
    },
    RES_REVOKE_MESSAGE_REPLY_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "res-revoke-message-reply-personal-aloline"
        }
    },

    /**
     * sticker personal
     * */
    CHAT_STICKER_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "chat-sticker-personal-aloline"
        }
    },
    RES_CHAT_STICKER_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "res-chat-sticker-personal-aloline"
        }
    },

    /**
     * update background personal
     * */
    UPDATE_BACKGROUND_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "update-background-personal-aloline"
        }
    },
    RES_UPDATE_BACKGROUND_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "res-update-background-personal-aloline"
        }
    },

    /**
     * Business Card
     * */
    BUSINESS_CARD_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "business-card-personal-aloline"
        }
    },
    RES_BUSINESS_CARD_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "res-business-card-personal-aloline"
        }
    },
    RES_CHAT_BIRTHDAY_ALOLINE {
        override fun toString(): String {
            return "res-chat-birthday-aloline"
        }
    },
    RES_CHAT_INVITE_CARD_ALOLINE {
        override fun toString(): String {
            return "res-chat-invite-card-personal-aloline"
        }
    },

    /**
     * Message Important
     * */
    STAR_MESSAGE_ALO_LINE {
        override fun toString(): String {
            return "star-message-aloline"
        }
    },
    RES_STAR_MESSAGE_ALO_LINE {
        override fun toString(): String {
            return "res-star-message-aloline"
        }
    },
    STAR_MESSAGE_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "star-message-personal-aloline"
        }
    },
    RES_STAR_MESSAGE_PERSONAL_ALO_LINE {
        override fun toString(): String {
            return "res-star-message-personal-aloline"
        }
    },

    /**
     * conversation type
     * */
    PERSONAL {
        override fun toString(): String {
            return "TWO_PERSONAL"
        }
    },
    GROUP {
        override fun toString(): String {
            return "GROUP"
        }
    },

    /**
     * Reaction type
     * */

    TYPE_REACTION_WOW {
        override fun toString(): String {
            return "6"
        }
    },
    TYPE_REACTION_ANGRY {
        override fun toString(): String {
            return "5"
        }
    },
    TYPE_REACTION_SAD {
        override fun toString(): String {
            return "4"
        }
    },
    TYPE_REACTION_LIKE {
        override fun toString(): String {
            return "3"
        }
    },
    TYPE_REACTION_SMILE {
        override fun toString(): String {
            return "2"
        }
    },
    TYPE_REACTION_LOVE {
        override fun toString(): String {
            return "1"
        }
    },

    /**
     * Number message not seen
     * */
    MESSAGE_NOT_SEEN_BY_ALL_GROUP {
        override fun toString(): String {
            return "message-not-seen-by-all-group"
        }
    },

    /**
     * Information data
     * */
    MESSAGE_INFORMATION {
        override fun toString(): String {
            return "MESSAGE_INFORMATION"
        }
    },
    MEMBER_INFORMATION {
        override fun toString(): String {
            return "MEMBER_INFORMATION"
        }
    },
    MESSAGE_DIFF_UTIL {
        override fun toString(): String {
            return "MESSAGE_DIFF_UTIL"
        }
    },

    //keyboard
    TYPE_KEYBOARD_STICKER {
        override fun toString(): String {
            return "TYPE_KEYBOARD_STICKER"
        }
    },
    TYPE_KEYBOARD_UTILITIES {
        override fun toString(): String {
            return "TYPE_KEYBOARD_UTILITIES"
        }
    },
    CONTACT_DEVICE_CHAT {
        override fun toString(): String {
            return "CONTACT_DEVICE_CHAT"
        }
    },

    /**
     * Deadline Vote
     * */
    UNLIMITED_TIME {
        override fun toString(): String {
            return "unlimited"
        }
    },

    /**
     * Cache copy chat
     * */
    CACHE_COPY {
        override fun toString(): String {
            return "CACHE_COPY"
        }
    },

    /**
     * Cache keyboard height chat
     * */
    CACHE_KEYBOARD {
        override fun toString(): String {
            return "CACHE_KEYBOARD"
        }
    },

    /**
     * Cache suggest file
     * */
    CACHE_SUGGEST_FILE {
        override fun toString(): String {
            return "CACHE_SUGGEST_FILE"
        }
    },

    /**
     * Type Media
     * */
    TYPE_MEDIA {
        override fun toString(): String {
            return "TYPE_MEDIA"
        }
    },

    /**
     * Type Member
     * */
    TYPE_MEMBER_ADMIN {
        override fun toString(): String {
            return "ADMIN"
        }
    },
    TYPE_MEMBER {
        override fun toString(): String {
            return "MEMBER"
        }
    },
    TYPE_MEMBER_GROUP_VICE {
        override fun toString(): String {
            return "GROUP_VICE"
        }
    },

    /**
     * disconnect/connect user
     * */
    CLIENT_DISCONNECTION_ALO_LINE {
        override fun toString(): String {
            return "client-disconnection-aloline"
        }
    },
    CLIENT_CONNECTION_ALO_LINE {
        override fun toString(): String {
            return "client-connection-aloline"
        }
    },
    CLIENT_CONNECTION_ROOM_ALO_LINE {
        override fun toString(): String {
            return "join-room-for-connection-aloline"
        }
    },
    CLIENT_DISCONNECTION_ROOM_ALO_LINE {
        override fun toString(): String {
            return "leave-room-for-connection-aloline"
        }
    },

    CHAT_INVITE_CARD_ALOLINE {
        override fun toString(): String {
            return "chat-invite-card-personal-aloline"
        }
    },

    /**
     * Background
     * */
    CACHE_BACKGROUND {
        override fun toString(): String {
            return "CACHE_BACKGROUND"
        }
    },
    MEMBER_ID {
        override fun toString(): String {
            return "MEMBER_ID"
        }
    },
    USER_INVITE_ID {
        override fun toString(): String {
            return "USER_INVITE_ID"
        }
    },
    INVITE_EVENT {
        override fun toString(): String {
            return "invite_event"
        }
    },
    RES_LOG_OUT_ALOLINE {
        override fun toString(): String {
            return "res-log-out-aloline"
        }
    },
    LIST_IMAGE_SUGGET {
        override fun toString(): String {
            return "LIST_IMAGE_SUGGET"
        }
    },
    RES_CHAT_GIFT_PERSONAL_ALOLINE {
        override fun toString(): String {
            return "res-chat-gift-personal-aloline"
        }
    },
    RES_LIST_MESSAGE_WHEN_USER_ONLINE {
        override fun toString(): String {
            return "res-list-message-when-user-online"
        }
    },
    /**
     * Last message group
     * */
    LAST_MESSAGE_GROUP {
        override fun toString(): String {
            return "LAST_MESSAGE_GROUP"
        }
    },
    NUM_MEMBER {
        override fun toString(): String {
            return "NUM_MEMBER"
        }
    },
}