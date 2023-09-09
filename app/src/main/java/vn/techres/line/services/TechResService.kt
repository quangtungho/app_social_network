package vn.techres.line.services

import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import vn.techres.line.data.local.contact.bestfriend.BestFriendResponse
import vn.techres.line.data.local.contact.friendonline.FriendOnlineResponse
import vn.techres.line.data.local.contact.friendrequestcontact.FriendRequestContactResponse
import vn.techres.line.data.local.contact.myfriend.MyFriendResponse
import vn.techres.line.data.local.contact.newfriend.NewFriendResponse
import vn.techres.line.data.model.account.InformationLoginResponse
import vn.techres.line.data.model.address.response.AddressResponse
import vn.techres.line.data.model.address.response.CitiesResponse
import vn.techres.line.data.model.address.response.DistrictsResponse
import vn.techres.line.data.model.address.response.WardResponse
import vn.techres.line.data.model.album.params.AlbumParams
import vn.techres.line.data.model.album.params.ImageAlbumParams
import vn.techres.line.data.model.album.params.ShareAlbumParams
import vn.techres.line.data.model.album.params.UpdateNameAlbumParams
import vn.techres.line.data.model.album.response.AlbumResponse
import vn.techres.line.data.model.album.response.CreateAlbumResponse
import vn.techres.line.data.model.album.response.ImageAlbumResponse
import vn.techres.line.data.model.avert.AdvertHistoryResonse
import vn.techres.line.data.model.bill.FoodVATResponse
import vn.techres.line.data.model.booking.CountGiftBookingResponse
import vn.techres.line.data.model.branch.response.BranchResponse
import vn.techres.line.data.model.branch.response.ListNewBranchResponse
import vn.techres.line.data.model.branch.response.SaveBranchResponse
import vn.techres.line.data.model.card.DenominationCardResponse
import vn.techres.line.data.model.chat.GroupsResponse
import vn.techres.line.data.model.chat.params.ViceGroupParams
import vn.techres.line.data.model.chat.response.*
import vn.techres.line.data.model.contact.response.ContactChatResponse
import vn.techres.line.data.model.deliveries.DeliveriesResponse
import vn.techres.line.data.model.food.reponse.FoodBestSellerResponse
import vn.techres.line.data.model.friend.FriendSuggestResponse
import vn.techres.line.data.model.friend.response.FriendResponse
import vn.techres.line.data.model.friend.response.SearchFriendResponse
import vn.techres.line.data.model.game.drink.request.CheckPlayTimesRequest
import vn.techres.line.data.model.game.drink.request.ChooseDrinkRequest
import vn.techres.line.data.model.game.drink.response.ChooseDrinkResponse
import vn.techres.line.data.model.game.drink.response.PlayTimesResponse
import vn.techres.line.data.model.gift.ConfirmBookingGiftParams
import vn.techres.line.data.model.gift.GiftDetailResponse
import vn.techres.line.data.model.gift.GiftListResponse
import vn.techres.line.data.model.gift.OpenListGiftParams
import vn.techres.line.data.model.mediaprofile.MediaAlbumResponse
import vn.techres.line.data.model.mediaprofile.MediaProfileResponse
import vn.techres.line.data.model.notification.StatusNotifyResponse
import vn.techres.line.data.model.params.*
import vn.techres.line.data.model.profile.response.UpdateProfileResponse
import vn.techres.line.data.model.reaction.InteractiveUserResponse
import vn.techres.line.data.model.reaction.ValueReactionResponse
import vn.techres.line.data.model.request.DataPostedRequest
import vn.techres.line.data.model.response.*
import vn.techres.line.data.model.response.game.*
import vn.techres.line.data.model.restaurant.response.RestaurantCardDetailResponse
import vn.techres.line.data.model.restaurant.response.RestaurantCardLevelResponse
import vn.techres.line.data.model.restaurant.response.RestaurantCardResponse
import vn.techres.line.data.model.restaurant.response.RestaurantRegisterResponse
import vn.techres.line.data.model.stranger.params.PermissionStrangerParams
import vn.techres.line.data.model.stranger.response.PermissionStrangerResponse
import vn.techres.line.data.model.version.response.VersionResponse
import vn.techres.line.data.model.voucher.VoucherDetailResponse
import vn.techres.line.data.model.voucher.VoucherResponse

const val BASE_PARAMS = "/api/queues"

interface TechResService {

    @POST(BASE_PARAMS)
    fun getDetailBranch(
        @Body params: BaseParams
    ): Observable<DetailBranchResponse>

    @POST(BASE_PARAMS)
    fun getListReviewBranch(
        @Body params: ListReviewBranchParam
    ): Observable<ReviewBranchResponse>

    @POST(BASE_PARAMS)
    fun createReviewBranch(@Body params: ReviewBranchParams): Observable<DataPostedResponse>

    @POST(BASE_PARAMS)
    fun removePostDraft(@Body params: RemovePostDraftParams): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun getCommentReviewBranch(@Body params: BaseParams): Observable<CommentReviewBranchResponse>

    @POST(BASE_PARAMS)
    fun getDetailReviewBranch(@Body params: BaseParams): Observable<OneReviewBranchReponse>

    @POST(BASE_PARAMS)
    fun createCommentReview(
        @Body params: CommentParams
    ): Observable<CreateCommentResponse>

    @POST(BASE_PARAMS)
    fun updateReviewBranch(
        @Body params: ReviewBranchEditReviewParams
    ): Observable<DataPostedResponse>

    @POST(BASE_PARAMS)
    fun deleteCommentReviewBranch(
        @Body params: DeleteCommentReplyParams
    ): Observable<BaseResponse>


    @POST(BASE_PARAMS)
    fun editCommentBranch(
        @Body parmas: CommentReviewBranchEditParams
    ): Observable<CreateCommentResponse>


    @POST(BASE_PARAMS)
    fun getMenu(
        @Body params: BaseParams
    ): Observable<MenuResponse>

    @POST(BASE_PARAMS)
    fun getConfig(
        @Body configParams: ConfigParams
    ): Observable<ConfigResponse>

    @POST(BASE_PARAMS)
    fun login(
        @Body userParams: UserParams
    ): Observable<UserResponse> // body data

    @POST(BASE_PARAMS)
    fun externalLogin(
        @Body params: LoginTypeParams
    ): Observable<UserResponse> // body data

    @POST(BASE_PARAMS)
    fun createBooking(
        @Body params: CreateBookingParams
    ): Observable<CreateBookingResponse>

    @POST(BASE_PARAMS)
    fun getFoodsPoint(
        @Body params: BaseParams
    ): Observable<FoodPointResponse>


    @POST(BASE_PARAMS)
    fun getCommentNewFeed(
        @Body params: BaseParams
    ): Observable<CommentNewFeedResponse>

    @POST(BASE_PARAMS)
    fun updateProfile(
        @Body params: UpdateProfileParams
    ): Observable<UpdateProfileResponse>

    @POST(BASE_PARAMS)
    fun getProfile(
        @Body params: BaseParams
    ): Observable<UpdateProfileResponse>

    @POST(BASE_PARAMS)
    fun getWards(@Body params: BaseParams): Observable<WardResponse>

    @POST(BASE_PARAMS)
    fun getDistricts(@Body params: BaseParams): Observable<DistrictsResponse>

    @POST(BASE_PARAMS)
    fun getCities(@Body baseParams: BaseParams): Observable<CitiesResponse>

    @POST(BASE_PARAMS)
    fun createCommentNewFeed(
        @Body params: CommentNewFeedParams
    ): Observable<OneCommentNewFeedResponse>

    @POST(BASE_PARAMS)
    fun getDetailNewFeed(
        @Body params: BaseParams
    ): Observable<DetailNewFeedResponse>

    @POST(BASE_PARAMS)
    fun deleteCommentDetailNewFeed(
        @Body params: BaseParams
    ): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun feedbackForDevelopers(
        @Body params: FeedbackForDevelopersParams
    ): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun register(@Body params: RegistryRequestParams): Observable<BaseResponse>


    @POST(BASE_PARAMS)
    fun resentCode(@Body params: UserParams): Observable<BaseResponse>


    @POST(BASE_PARAMS)
    fun editCommentNewFeed(
        @Body params: CommentNewNewParams
    ): Observable<OneCommentNewFeedResponse>

    @POST(BASE_PARAMS)
    fun getListNewBranch(@Body params: BaseParams): Observable<ListNewBranchResponse>

    @POST(BASE_PARAMS)
    fun getInforBirthdayGift(@Body params: BaseParams): Observable<InforBirthdayGiftResponse>

    @POST(BASE_PARAMS)
    fun getDetailIsTakeAway(@Body params: BaseParams): Observable<IsTakeAwayRespone>

    @POST(BASE_PARAMS)
    fun changedPassword(
        @Body changedPasswordData: ChangedPasswordParams
    ): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun forgotPassword(@Body forgotPasswordParams: ForgotPasswordParams): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun verifyResetPassword(@Body params: ReVerifyCodePhoneParams): Observable<VerifyResetPasswordResponse>

    @POST(BASE_PARAMS)
    fun verifyPasswordAfterLoginFacebook(@Body params: ReVerifyCodePhoneParams): Observable<VerifyResetPasswordResponse>

    @POST(BASE_PARAMS)
    fun getListBranch(
        @Body params: BaseParams
    ): Observable<BranchResponse>

    @POST(BASE_PARAMS)
    fun getListBranchGift(
        @Body params: BaseParams
    ): Observable<BranchGiftResponse>

    @POST(BASE_PARAMS)
    fun getFoodTakeAway(
        @Body params: BaseParams
    ): Observable<FoodTakeAwayResponse>

    @POST(BASE_PARAMS)
    fun getBillTable(
        @Body params: BaseParams
    ): Observable<BillTableResponse>

    @POST(BASE_PARAMS)
    fun creatComFormOrder(
        @Body params: ItemCartParams
    ): Observable<ComFormOrderResponse>

    @POST(BASE_PARAMS)
    fun getAddress(@Body params: BaseParams): Observable<AddressResponse>

    @POST(BASE_PARAMS)
    fun updateAddress(
        @Body params: ShippingAddressParams
    ): Observable<ShippingAddressResponse>


    @POST(BASE_PARAMS)
    fun createOnlineOrder(
        @Body params: CreateOnlineOrderParams
    ): Observable<OrderInvoiceResponse>

    @POST(BASE_PARAMS)
    fun deletelAddress(
        @Body params: DeleteAddressParams
    ): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun getOrderCustomer(
        @Body params: BaseParams
    ): Observable<OrderCustomerResponse>

    @POST(BASE_PARAMS)
    fun getOrderCustomer(@Body params: OrderCustomerParams): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun barCodeRequest(@Body params: BarCodeParams): Observable<RestaurantCardDetailResponse>

    @POST(BASE_PARAMS)
    fun usingPointToOrder(
        @Body usingPointToOrderParams: UsingPointToOrderParams
    ): Observable<BillTableResponse>

    @POST(BASE_PARAMS)
    fun getRestaurantCard(@Body params: BaseParams): Observable<RestaurantCardResponse>

    @POST(BASE_PARAMS)
    fun getListMemberCard(
        @Body params: ListPointHistoryParams
    ): Observable<DetailsMemberCardResponse>

    @POST(BASE_PARAMS)
    fun getListProcessingBooking(@Body params: BaseParams): Observable<BookingResponse>

    @POST(BASE_PARAMS)
    fun getListProcessedBooking(@Body params: BaseParams): Observable<BookingResponse>

    @POST(BASE_PARAMS)
    fun cancelBooking(
        @Body params: CancelBookingParams
    ): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun createReceiveBirthday(
        @Body params: CreateReceiveBirthdayParams
    ): Observable<BaseResponse>


    @POST(BASE_PARAMS)
    fun getNotifications(
        @Body params: BaseParams
    ): Observable<NotificationResponse>

    @POST(BASE_PARAMS)
    fun MarkNotifications(@Body params: BaseParams): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun DeleteNotifications(@Body params: BaseParams): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun sendPushToken(@Body params: PushTokenParams): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun orderFoodByPoint(
        @Body params: OrderFoodByPointParams
    ): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun createUserInformationNode(@Body params: CreateUserInformationNodeParams): Observable<BaseResponse>


    @POST(BASE_PARAMS)
    fun pushToken(@Body params: PushTokenNodeParams): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun removePost(
        @Body params: BaseParams
    ): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun getGroups(
        @Body params: vn.techres.line.data.model.chat.params.GroupParams
    ): Observable<GroupsResponse>

    @POST(BASE_PARAMS)
    fun createGroup(
        @Body params: GroupParams
    ): Observable<CreateGroupResponse>


    @POST(BASE_PARAMS)
    fun getMessages(
        @Body params: BaseParams
    ): Observable<MessagesResponse>

    @POST(BASE_PARAMS)
    fun deleteGroup(
        @Body params: BaseParams
    ): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun getCategorySticker(@Body params: BaseParams): Observable<CategoryStickerResponse>

    @POST(BASE_PARAMS)
    fun getAvatarGroup(
        @Body params: BaseParams
    ): Observable<FileNodeJsResponse>

    @POST(BASE_PARAMS)
    fun getImageChat(
        @Body params: BaseParams
    ): Observable<FileNodeJsResponse>

    @POST(BASE_PARAMS)
    fun getBackground(
        @Body params: BaseParams
    ): Observable<BackgroundResponse>

    @POST(BASE_PARAMS)
    fun getMemberGroup(@Body params: BaseParams): Observable<MemberGroupResponse>

    @POST(BASE_PARAMS)
    fun addNewUser(
        @Body params: AddNewUserParams
    ): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun getLinkImageNewFeed(
        @Body params: BaseParams
    ): Observable<ImageResponse>

    @POST(BASE_PARAMS)
    fun getLinkImageReviewRestaurent(
        @Body params: BaseParams
    ): Observable<ImageResponse>

    @POST(BASE_PARAMS)
    fun getMediaChat(
        @Body params: BaseParams
    ): Observable<MediaChatResponse>

    @POST(BASE_PARAMS)
    fun logoutToken(@Body params: BaseParams): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun authorizeMember(
        @Body params: AuthorizeAdminParams
    ): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun getFoodBestSellerBranch(
        @Body params: BaseParams
    ): Observable<FoodBestSellerResponse>

    @POST(BASE_PARAMS)
    fun getListFriend(
        @Body params: BaseParams
    ): Observable<MyFriendResponse>


    @POST(BASE_PARAMS)
    fun getListFriendRequest(
        @Body params: BaseParams
    ): Observable<FriendResponse>

    @POST(BASE_PARAMS)
    fun postCustomerAdvertContracts(
        @Body params: AdvertContractParams
    ): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun saveBranch(
        @Body params: BaseParams
    ): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun getSaveBranch(
        @Body params: BaseParams
    ): Observable<SaveBranchResponse>

    @POST(BASE_PARAMS)
    fun detailBranch(
        @Body params: BaseParams
    ): Observable<DetailCommentResponse>


    @POST(BASE_PARAMS)
    fun updateRestaurantId(
        @Body params: UserNodejsParams
    ): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun getListGame(
        @Body params: BaseParams
    ): Observable<ListGameResponse>

//    @POST("/api/room-lucky-wheel/get-list-room")
//    fun getListRoomGame(
//        @Query("id_article_game") id_article_game: String,
//        @Query("restaurant_id") restaurant_id: Int,
//        @Query("current_type") current_type: String
//    ): Observable<ListroomGameResponse>

    @POST(BASE_PARAMS)
    fun getListRoomGame(
        @Body params: ListRoomGameParams
    ): Observable<ListroomGameResponse>

    @POST(BASE_PARAMS)
    fun checkRoom(@Body params: QRCodeGameParams): Observable<JoinGameResponse>

    @POST(BASE_PARAMS)
    fun getConfigsNodeJs(
        @Body configsNodeJsParams: ConfigsNodeJsParams
    ): Observable<ConfigNodeJsResponse>

    @POST(BASE_PARAMS)
    fun getMessagePaginationGameLuckyWheel(
        @Body params: BaseParams
    ): Observable<MessageGameLuckyWheelResponse>

    @POST(BASE_PARAMS)
    fun createChatPersonal(
        @Body params: GroupPersonalParams
    ): Observable<GroupPersonalResponse>

    @POST(BASE_PARAMS)
    fun getListUserWinner(
        @Body params: UserWinnerParams
    ): Observable<UserWinnerResponse>

    @POST(BASE_PARAMS)
    fun getPinnedDetail(
        @Body params: BaseParams
    ): Observable<PinnedResponse>

    @POST(BASE_PARAMS)
    fun removePinned(
        @Body params: BaseParams
    ): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun likeUnlikeRestaurantSocialContents(@Body params: BaseParams): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun getRestaurantNearby(
        @Body restaurantNearbyParams: RestaurantNearbyParams
    ): Observable<RestaurantRegisterResponse>

    @POST(BASE_PARAMS)
    fun getAdverttHome(
        @Body params: BaseParams
    ): Observable<AdvertResponse>

    @POST("/api/beverages-game/get-beverages-game")
    fun getDrinkGame(
        @Query("restaurant_id") restaurant_id: Int,
        @Query("branch_id") branch_id: Int,
        @Query("current_type") current_type: String
    ): Observable<DrinkGameResponse>

    @POST("/api/beverages-game/inc-total-vote")
    fun chooseDrink(
        @Body chooseDrinkRequest: ChooseDrinkRequest
    ): Observable<ChooseDrinkResponse>

    @POST("/api/beverages-game/check-total-round")
    fun getPlayTimes(
        @Body checkPlayTimesRequest: CheckPlayTimesRequest
    ): Observable<PlayTimesResponse>

    @POST(BASE_PARAMS)
    fun postReactionReview(@Body postReactionReviewParams: PostReactionReviewParams): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun postReactionComment(@Body postReactionCommentParams: PostReactionCommentParams): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun checkUpdate(
        @Body params: BaseParams
    ): Observable<VersionResponse>

    @POST(BASE_PARAMS)
    fun kickMemberGroup(
        @Body params: KickMemberParams
    ): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun updateGroup(
        @Body params: UpdateGroupParams
    ): Observable<BaseResponse>


    @POST(BASE_PARAMS)
    fun getReviewReactionSummary(@Body baseParams: BaseParams): Observable<ReactionSummaryReviewResponse>

    @POST(BASE_PARAMS)
    fun getReviewReactionDetail(@Body baseParams: BaseParams): Observable<ReactionPostResponse>

    @POST(BASE_PARAMS)
    fun getMessagesPersonal(
        @Body params: BaseParams
    ): Observable<MessagesResponse>

    @POST(BASE_PARAMS)
    fun getMessagesNotSeenAllGroup(@Body params: BaseParams): Observable<NumberMessageAllGroupResponse>

    @POST(BASE_PARAMS)
    fun getRestaurantCardLevel(
        @Body params: BaseParams
    ): Observable<RestaurantCardLevelResponse>

    @POST(BASE_PARAMS)
    fun getRestaurantSlogan(
        @Body params: BaseParams
    ): Observable<RestaurantSloganResponse>

    @POST(BASE_PARAMS)
    fun getListOrderReview(
        @Body params: BaseParams
    ): Observable<OrderReviewResponse>

    @POST(BASE_PARAMS)
    fun createReviewOrder(
        @Body params: OrderReviewParams
    ): Observable<BaseResponse>


    @POST(BASE_PARAMS)
    fun getLinkMedia(
        @Body baseRequest: BaseParams
    ): Observable<LinkMediaChatResponse>

    @POST(BASE_PARAMS)
    fun getRestaurantCardDetail(@Body params: BaseParams): Observable<RestaurantCardDetailResponse>

    @POST(BASE_PARAMS)
    fun getOnePinned(
        @Body params: BaseParams
    ): Observable<PinnedOneResponse>

    @POST(BASE_PARAMS)
    fun getMessageGroup(
        @Body params: BaseParams
    ): Observable<SearchMessageResponse>

    @POST(BASE_PARAMS)
    fun getMessagePersonal(
        @Body params: BaseParams
    ): Observable<SearchMessageResponse>

    @POST(BASE_PARAMS)
    fun getMediaProfile(
        @Body params: BaseParams
    ): Observable<MediaProfileResponse>

    @POST(BASE_PARAMS)
    fun createCustomerMediaRequest(@Body params: CreateCustomerMediaParams): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun getListAlbum(
        @Body params: BaseParams
    ): Observable<MediaAlbumResponse>

    @POST(BASE_PARAMS)
    fun createAlbum(@Body params: CreateAlbumParams): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun checkStatusProfile(
        @Body params: BaseParams
    ): Observable<StatusProfileResponse>

    @POST(BASE_PARAMS)
    fun createContact(
        @Body params: ContactParams
    ): Observable<BaseResponse>


    @POST(BASE_PARAMS)
    fun createContactTogether(@Body params: BaseParams): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun getContact(
        @Body params: BaseParams
    ): Observable<FriendResponse>

    @POST(BASE_PARAMS)
    fun deleteImageMedia(@Body params: BaseParams): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun dissolutionGroup(
        @Body params: BaseParams
    ): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun getSticker(
        @Body params: BaseParams
    ): Observable<CategoryStickerResponse>

    @POST(BASE_PARAMS)
    fun getDetailGroup(
        @Body params: BaseParams
    ): Observable<DetailGroupResponse>

    @POST(BASE_PARAMS)
    fun getOneGroup(
        @Body params: BaseParams
    ): Observable<OneGroupResponse>


    @POST(BASE_PARAMS)
    fun getFriendsNew(
        @Body params: BaseParams
    ): Observable<NewFriendResponse>

    @POST(BASE_PARAMS)
    fun updateRestaurant(
        @Body params: UpdateRestaurantParams
    ): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun addFriend(
        @Body params: FriendParams
    ): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun acceptFriend(
        @Body params: FriendAcceptParams
    ): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun notAcceptFriend(
        @Body params: FriendParams
    ): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun revokeFriend(
        @Body params: FriendParams
    ): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun unFriend(
        @Body params: FriendParams
    ): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun getListFriendOnline(
        @Body params: BaseParams
    ): Observable<FriendOnlineResponse>

    @POST(BASE_PARAMS)
    fun getListBestFriend(
        @Body params: BaseParams
    ): Observable<BestFriendResponse>

    @POST(BASE_PARAMS)
    fun addBestFriend(
        @Body params: AddBestFriendParams
    ): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun getListFriendNotBestFriend(
        @Body params: BaseParams
    ): Observable<FriendResponse>

    @POST(BASE_PARAMS)
    fun getFriendTotalRequest(@Body baseRequest: BaseParams): Observable<FriendRequestContactResponse>


    @POST(BASE_PARAMS)
    fun getCountGift(@Body baseRequest: BaseParams): Observable<CountGiftBookingResponse>

    @POST(BASE_PARAMS)
    fun searchAddFriend(
        @Body searchFriendParams: SearchFriendParams
    ): Observable<SearchFriendResponse>

    @POST(BASE_PARAMS)
    fun getLevelValue(@Body baseRequest: BaseParams): Observable<LevelValueResponse>

    @POST(BASE_PARAMS)
    fun onOffNotifyChat(
        @Body params: NotifyChatParams
    ): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun setTurnNotify(
        @Body params: NotifyAppParams
    ): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun getStatusNotify(@Body baseRequest: BaseParams): Observable<StatusNotifyResponse>

    @POST(BASE_PARAMS)
    fun getSearchListReviewBranch(
        @Body baseRequest: BaseParams
    ): Observable<ReviewBranchResponse>

    @POST(BASE_PARAMS)
    fun getFriendNotInGroup(
        @Body baseRequest: BaseParams
    ): Observable<FriendResponse>

    @POST(BASE_PARAMS)
    fun getRestaurantMember(
        @Body baseParams: BaseParams
    ): Observable<FriendResponse>

    @POST(BASE_PARAMS)
    fun getFriendSuggest(
        @Body baseParams: BaseParams
    ): Observable<FriendSuggestResponse>

    @POST(BASE_PARAMS)
    fun confirmBookingFoodGift(
        @Body confirmBookingGiftParams: ConfirmBookingGiftParams
    ): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun updateAvatar(
        @Body params: UpdateAvatarParams
    ): Observable<UpdateProfileResponse>

    @POST(BASE_PARAMS)
    fun getMessageViewer(
        @Body params: MessageViewerParams
    ): Observable<MessageViewerResponse>

    @POST(BASE_PARAMS)
    fun getValueReation(
        @Body params: BaseParams
    ): Observable<ValueReactionResponse>

    @POST(BASE_PARAMS)
    fun leaveGroup(
        @Body params: BaseParams
    ): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun createReplyComment(
        @Body params: CommentParams
    ): Observable<CreateCommentResponse>

    @POST(BASE_PARAMS)
    fun getDenominations(
        @Body params: BaseParams
    ): Observable<DenominationCardResponse>

    @POST(BASE_PARAMS)

    fun getDenominationsss(
        @Body params: BaseParams
    ): DenominationCardResponse

    @POST(BASE_PARAMS)
    fun getListReactionComment(@Body postParams: ReactionCommentParams): Observable<InteractiveUserResponse>

    @POST(BASE_PARAMS)
    fun viceGroup(
        @Body params: ViceGroupParams
    ): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun getContactChat(
        @Body params: ContactChatParams
    ): Observable<ContactChatResponse>

    @POST(BASE_PARAMS)
    fun getListReaction(
        @Body params: BaseParams
    ): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun getMessageImportant(
        @Body params: BaseParams
    ): Observable<MessagesResponse>

    @POST(BASE_PARAMS)
    fun removeMessageImportant(
        @Body params: BaseParams
    ): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun callAPICustomerSettings(
        @Body params: BaseParams
    ): Observable<DeliveriesResponse>

    @POST(BASE_PARAMS)
    fun getPermissionStranger(
        @Body params: BaseParams
    ): Observable<PermissionStrangerResponse>

    @POST(BASE_PARAMS)
    fun setPermissionStranger(
        @Body params: PermissionStrangerParams
    ): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun getVoucher(
        @Body params: BaseParams
    ): Observable<VoucherResponse>

    @POST(BASE_PARAMS)
    fun getVoucherDetail(
        @Body params: BaseParams
    ): Observable<VoucherDetailResponse>

    @POST(BASE_PARAMS)
    fun getAdvert(
        @Body params: BaseParams
    ): Observable<AdvertResponse>

    @POST(BASE_PARAMS)
    fun getAdvertHistory(
        @Body params: BaseParams
    ): Observable<AdvertHistoryResonse>

    @POST(BASE_PARAMS)
    fun getGiftList(@Body params: BaseParams): Observable<GiftListResponse>

    @POST(BASE_PARAMS)
    fun getGiftDetail(
        @Body params: BaseParams
    ): Observable<GiftDetailResponse>

    @POST(BASE_PARAMS)
    fun postMarkViewedNotification(@Body params: BaseParams): Observable<NotificationsResponse>

    @POST(BASE_PARAMS)
    fun getDraftsPost(@Body params: DraftPostParams): Observable<DataDraftResponse>

    @POST(BASE_PARAMS)
    fun deleteDraftsPost(@Body params: BaseParams): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun postDraft(@Body params: DataPostedRequest): Observable<DataPostedResponse>

    @POST(BASE_PARAMS)
    fun removeNotification(@Body params: BaseParams): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun createFolderAlbum(@Body params: AlbumParams): Observable<CreateAlbumResponse>

    @POST(BASE_PARAMS)
    fun updateNameFolderAlbum(@Body params: UpdateNameAlbumParams): Observable<CreateAlbumResponse>

    @POST(BASE_PARAMS)
    fun getFolderAlbum(@Body params: BaseParams): Observable<AlbumResponse>

    @POST(BASE_PARAMS)
    fun removeFolderAlbum(@Body params: BaseParams): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun getImageToAlbum(@Body params: ImageAlbumParams): Observable<ImageAlbumResponse>

    @POST(BASE_PARAMS)
    fun removeImageFromAlbum(@Body params: BaseParams): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun getBirthDayCard(@Body params: BaseParams): Observable<BirthDayCardResponse>

    @POST(BASE_PARAMS)
    fun shareAlbum(@Body params: ShareAlbumParams): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun cancelShareAlbum(@Body params: BaseParams): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun getCategoryMediaRestaurant(@Body params: BaseParams): Observable<CategoryMediaRestaurantResponse>

    @POST(BASE_PARAMS)
    fun getMediaRestaurant(@Body params: BaseParams): Observable<ImageAlbumResponse>

    @POST(BASE_PARAMS)
    fun getListFoodVAT(@Body params: BaseParams): Observable<FoodVATResponse>

    @POST(BASE_PARAMS)
    fun getLastUserLogin(@Body params: BaseParams): Observable<InformationLoginResponse>

    @POST(BASE_PARAMS)
    fun getListGift(
        @Body params: OpenListGiftParams
    ): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun getRestaurantBranch(
        @Body params: BaseParams
    ): Observable<RestaurantBranchResponse>

    @POST(BASE_PARAMS)
    fun getGroupsShare(
        @Body params: BaseParams
    ): Observable<GroupsShareResponse>
    @POST(BASE_PARAMS)
    fun getMessageViewed(
        @Body params: BaseParams
    ): Observable<MessageViewedDataRespone>

    @POST(BASE_PARAMS)
    fun postPostReport(@Body postReportParams: PostReportParams): Observable<BaseResponse>

    @POST(BASE_PARAMS)
    fun reportUser(@Body reportUserParams: ReportUserParams): Observable<BaseResponse>
}

