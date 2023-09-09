package vn.techres.line.helper.techresenum

enum class TechresEnumFriend {
    REQUEST_TO_CONTACT {
        override fun toString(): String {
            return "request-to-contact"
        }
    },
    APPROVE_TO_CONTACT {
        override fun toString(): String {
            return "approve-to-contact"
        }
    },
}