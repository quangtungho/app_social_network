package vn.techres.line.videocall;

import org.jetbrains.annotations.NotNull;

/**
 * @Author: Phạm Văn Nhân
 * @Date: 3/1/2022
 */

public enum SocketChatMessageTypeEnum {
    TEXT {
        @NotNull
        @Override
        public String toString() {
            return "1";
        }
    },
    IMAGE {
        @NotNull
        @Override
        public String toString() {
            return "2";
        }
    },

    FILE {
        @NotNull
        @Override
        public String toString() {
            return "3";
        }
    },

    STICKER {
        @NotNull
        @Override
        public String toString() {
            return "4";
        }
    },
    VIDEO {
        @NotNull
        @Override
        public String toString() {
            return "5";
        }
    },
    AUDIO {
        @NotNull
        @Override
        public String toString() {
            return "6";
        }
    },
    BACKGROUND {
        @NotNull
        @Override
        public String toString() {
            return "background";
        }
    },

    AVATAR {
        @NotNull
        @Override
        public String toString() {
            return "avatar";
        }
    },

    GROUP_NAME {
        @NotNull
        @Override
        public String toString() {
            return "groupName";
        }
    },

    KICK {
        @NotNull
        @Override
        public String toString() {
            return "19";
        }
    },
    LEAVE {
        @NotNull
        @Override
        public String toString() {
            return "member_leave_group";
        }
    },

    ADD {
        @NotNull
        @Override
        public String toString() {
            return "17";
        }
    },

    PINNED {
        @NotNull
        @Override
        public String toString() {
            return "13";
        }
    },

    UPDATE_GROUP {
        @NotNull
        @Override
        public String toString() {
            return "14";
        }
    },

    AUTHORIZE {
        @NotNull
        @Override
        public String toString() {
            return "authorize";
        }
    },

    REPLY {
        @NotNull
        @Override
        public String toString() {
            return "7";
        }
    },
    LINK {
        @NotNull
        @Override
        public String toString() {
            return "8";
        }
    },

    REVOKE {
        @NotNull
        @Override
        public String toString() {
            return "9";
        }
    },

    REVOKE_REPLY {
        @NotNull
        @Override
        public String toString() {
            return "10";
        }
    },

    REMOVE_PINNED {
        @NotNull
        @Override
        public String toString() {
            return "11";
        }
    },

    REACTION {
        @NotNull
        @Override
        public String toString() {
            return "reaction";
        }
    },

    SHARE {
        @NotNull
        @Override
        public String toString() {
            return "12";
        }
    },

    MEDIA {
        @NotNull
        @Override
        public String toString() {
            return "media";
        }
    },

    ERROR {
        @NotNull
        @Override
        public String toString() {
            return "error";
        }
    },

    CREATE_GROUP {
        @NotNull
        @Override
        public String toString() {
            return "create_group";
        }
    },

    CALL_AUDIO {
        @NotNull
        @Override
        public String toString() {
            return "22";
        }
    },

    CALL_VIDEO {
        @NotNull
        @Override
        public String toString() {
            return "21";
        }
    },
    CONTACT {
        @NotNull
        @Override
        public String toString() {
            return "23";
        }
    },
    ORDER {
        @NotNull
        @Override
        public String toString() {
            return "25";
        }
    },
    VOTE {
        @NotNull
        @Override
        public String toString() {
            return "27";
        }
    },
    VOTE_TEXT {
        @NotNull
        @Override
        public String toString() {
            return "28";
        }
    },
    UPDATE_AVATAR {
        @NotNull
        @Override
        public String toString() {
            return "15";
        }
    },
    KICK_ROLE {
        @NotNull
        @Override
        public String toString() {
            return "30";
        }
    },
}
