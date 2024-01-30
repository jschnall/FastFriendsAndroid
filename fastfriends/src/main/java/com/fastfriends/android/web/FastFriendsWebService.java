package com.fastfriends.android.web;

import java.util.List;

import com.fastfriends.android.model.Album;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.model.Comment;
import com.fastfriends.android.model.Contact;
import com.fastfriends.android.model.FitHistory;
import com.fastfriends.android.model.Friend;
import com.fastfriends.android.model.Conversation;
import com.fastfriends.android.model.Device;
import com.fastfriends.android.model.Event;
import com.fastfriends.android.model.EventSearchFilter;
import com.fastfriends.android.model.EventMember;
import com.fastfriends.android.model.MemberPage;
import com.fastfriends.android.model.Message;
import com.fastfriends.android.model.Page;
import com.fastfriends.android.model.Plan;
import com.fastfriends.android.model.PlanSearchFilter;
import com.fastfriends.android.model.Prediction;
import com.fastfriends.android.model.Profile;
import com.fastfriends.android.model.Resource;
import com.fastfriends.android.model.Tag;
import com.fastfriends.android.model.User;
import com.fastfriends.android.model.UserAttributeSet;
import com.fastfriends.android.model.UserStatus;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;

/**
 * Created by jschnall on 1/24/14.
 */
public interface FastFriendsWebService {
    // Authentication
    @FormUrlEncoded
    @POST("/users/")
    User createUser(@Field("first_name") String firstName,
                    @Field("last_name") String lastName,
                    @Field("email") String email,
                    @Field("password") String password,
                    @Field("birthday") String birthday,
                    @Field("gender") String gender,
                    @Field("display_name") String displayName,
                    @Field("client_id") String clientId,
                    @Field("client_secret") String clientSecret);

    @FormUrlEncoded
    @POST("/users/")
    User createSocialUser(@Field("first_name") String firstName,
                    @Field("last_name") String lastName,
                    @Field("email") String email,
                    @Field("social_service") String socialService,
                    @Field("social_id") String socialId,
                    @Field("access_token") String access_token,
                    @Field("birthday") String birthday,
                    @Field("gender") String gender,
                    @Field("display_name") String displayName,
                    @Field("client_id") String clientId,
                    @Field("client_secret") String clientSecret);

    @PUT("/users/{id}/")
    User editUser(@Path("id") long id, @Header("Authorization") String authorization,
                  @Body User user);

    @GET("/users/current/")
    User getCurrentUser(@Header("Authorization") String authorization);

    @GET("/users/status/")
    UserStatus getUserStatus(@Header("Authorization") String authorization);

    @FormUrlEncoded
    @POST("/users/forgot_password/")
    JsonObject forgotPassword(@Field("client_id") String clientId,
                        @Field("client_secret") String clientSecret,
                        @Field("email") String email);

    @FormUrlEncoded
    @POST("/o/token/")
    AuthToken refreshAuthToken(@Field("client_id") String clientId,
                          @Field("client_secret") String clientSecret,
                          @Field("grant_type") String grantType,
                          @Field("refresh_token") String refreshToken,
                          @Field("scope") String scope);

    @FormUrlEncoded
    @POST("/o/token/")
    AuthToken signIn(@Field("client_id") String clientId,
                     @Field("client_secret") String clientSecret,
                     @Field("grant_type") String grantType,
                     @Field("username") String username,
                     @Field("password") String password);

    @FormUrlEncoded
    @POST("/social_sign_in/")
    AuthToken socialSignIn(@Field("client_id") String clientId,
                        @Field("client_secret") String clientSecret,
                        @Field("grant_type") String grantType,
                        @Field("social_service") String socialService,
                        @Field("social_id") String socialId,
                        @Field("access_token") String accessToken);

    // Core service
    @GET("/tags/")
    List<Tag> listTags(@Header("Authorization") String authorization);

    @POST("/events/")
    Event addEvent(@Header("Authorization") String authorization, @Body Event event);

    @GET("/events/")
    Page<Event> listEvents(@Header("Authorization") String authorization,
                           @Query("page") String page, @Query("page_size") String pageSize,
                           @Query("category") String category, @Query("latitude") Double latitude,
                           @Query("longitude") Double longitude);

    @POST("/events/search/")
    Page<Event> searchEvents(@Header("Authorization") String authorization,
                             @Body EventSearchFilter searchFilter);

    @GET("/events/")
    Page<Event> listUserEvents(@Header("Authorization") String authorization,
                           @Query("page") String page, @Query("page_size") String pageSize,
                           @Query("ordering") String ordering, @Query("search") String searchText,
                           @Query("user") long userId);

    @PUT("/events/{id}/")
    Event editEvent(@Path("id") long id, @Header("Authorization") String authorization,
                    @Body Event event);

    @GET("/events/{id}/")
    Event getEvent(@Header("Authorization") String authorization, @Path("id") long id);

    @GET("/events/{id}/members/")
    MemberPage<EventMember> getEventMembers(@Header("Authorization") String authorization,
                                      @Path("id") long id, @Query("page") String page,
                                      @Query("page_size") String pageSize,
                                      @Query("status") String status);

    @PUT("/events/{id}/cancel/")
    JsonObject cancelEvent(@Path("id") long id, @Header("Authorization") String authorization);

    @FormUrlEncoded
    @PUT("/events/{id}/promo/")
    Event setEventPromo(@Path("id") long id, @Header("Authorization") String authorization,
                        @Field("resource") long resourceId);

    @FormUrlEncoded
    @PUT("/events/{id}/checkin/")
    JsonObject checkInEvent(@Header("Authorization") String authorization, @Path("id") long id,
                       @Field("latitude") double latitude, @Field("longitude") double longitude);

    @PUT("/events/{id}/join/")
    EventMember joinEvent(@Header("Authorization") String authorization, @Path("id") long id);

    @PUT("/events/{id}/leave/")
    JsonObject leaveEvent(@Header("Authorization") String authorization, @Path("id") long id);

    @FormUrlEncoded
    @PUT("/events/{id}/invite/")
    JsonObject inviteToEvent(@Header("Authorization") String authorization, @Path("id") long eventId,
                              @Field("users") String userIds);

    @POST("/events/{id}/comment/")
    Comment addEventComment(@Header("Authorization") String authorization, @Path("id") long id,
                          @Body Comment comment);

    @POST("/plans/{id}/comment/")
    Comment addPlanComment(@Header("Authorization") String authorization, @Path("id") long id,
                            @Body Comment comment);

    @FormUrlEncoded
    @PUT("/event_members/{id}/approve/")
    JsonObject approveEventMember(@Header("Authorization") String authorization, @Path("id") long id,
                                  @Field("accept") boolean acceptMember);

    @FormUrlEncoded
    @PUT("/event_members/{id}/accept_invite/")
    JsonObject acceptInvite(@Header("Authorization") String authorization, @Path("id") long id,
                                  @Field("accept") boolean accept);

    @PUT("/comments/{id}/")
    Comment editComment(@Path("id") long id, @Header("Authorization") String authorization,
                    @Body Comment comment);

    @DELETE("/comments/{id}/")
    JsonObject deleteComment(@Path("id") long id, @Header("Authorization") String authorization);

    @GET("/comments/")
    Page<Comment> listEventComments(@Header("Authorization") String authorization,
                                    @Query("page") String page, @Query("page_size") String pageSize,
                                    @Query("event") long eventId);

    @GET("/comments/")
    Page<Comment> listPlanComments(@Header("Authorization") String authorization,
                               @Query("page") String page, @Query("page_size") String pageSize,
                               @Query("plan") long planId);

    @GET("/profiles/{id}/")
    Profile getProfile(@Path("id") long id, @Header("Authorization") String authorization);

    @GET("/profiles/")
    Page<Profile> getProfilesWithName(@Path("id") long id, @Header("Authorization") String authorization,
                                      @Query("name") String displayName);

    @PUT("/profiles/{id}/")
    Profile editProfile(@Path("id") long id, @Header("Authorization") String authorization,
                        @Body Profile profile);

    @GET("/profiles/{id}/friends/")
    Page<Friend> getMutualFriends(@Path("id") long id, @Header("Authorization") String authorization,
                                  @Query("page") String page, @Query("page_size") String pageSize);

    @FormUrlEncoded
    @PUT("/profiles/{id}/portrait/")
    Profile setProfilePortrait(@Path("id") long id, @Header("Authorization") String authorization,
                               @Field("resource") long resourceId);

    // Messaging
    @GET("/conversations/")
    Page<Conversation> listConversations(@Header("Authorization") String authorization,
                                    @Query("page") String page, @Query("page_size") String pageSize,
                                    @Query("category") String category);

    @FormUrlEncoded
    @PUT("/conversations/open/")
    JsonObject setConversationOpened(@Header("Authorization") String authorization,
                                  @Field("user") long userId);

    @FormUrlEncoded
    @PUT("/conversations/delete/")
    JsonObject setConversationDeleted(@Header("Authorization") String authorization,
                                     @Field("users") String userIds);

    @GET("/messages/")
    Page<Message> listMessages(@Header("Authorization") String authorization,
                                    @Query("page") String page, @Query("page_size") String pageSize,
                                    @Query("user") long userId);

    @POST("/messages/")
    Message addMessage(@Header("Authorization") String authorization, @Body Message message);

    @PUT("/drafts/")
    Message saveDraft(@Header("Authorization") String authorization, @Body Message message);

    @DELETE("/drafts/")
    JsonObject deleteDrafts(@Header("Authorization") String authorization, @Query("users") String userIds);

    @PUT("/messages/{id}/")
    Message editMessage(@Path("id") long id, @Header("Authorization") String authorization,
                        @Body Message message);

    @DELETE("/messages/{id}/")
    int deleteMessage(@Path("id") long id, @Header("Authorization") String authorization);

    @Multipart
    @POST("/resources/")
    Resource addResource(@Header("Authorization") String authorization, @Part("data") TypedFile data, @Part("album") TypedString albumId);

    @DELETE("/resources/{id}/")
    int deleteResource(@Path("id") long id, @Header("Authorization") String authorization);

    @FormUrlEncoded
    @PUT("/resources/{id}/caption/")
    Resource setResourceCaption(@Path("id") long id, @Header("Authorization") String authorization, @Field("caption") String caption);

    @DELETE("/resources/delete/")
    JsonObject deleteResources(@Header("Authorization") String authorization, @Query("resources") String resourceIds);

    @GET("/albums/")
    Page<Album> listEventAlbums(@Header("Authorization") String authorization,
                           @Query("page") String page, @Query("page_size") String pageSize,
                           @Query("event") long eventId);

    @GET("/albums/")
    Page<Album> listUserAlbums(@Header("Authorization") String authorization,
                                @Query("page") String page, @Query("page_size") String pageSize,
                                @Query("owner") long userId);

    @PUT("/albums/{id}/")
    Album editAlbum(@Path("id") long id, @Header("Authorization") String authorization,
                            @Body Album album);

    @GET("/place/autocomplete/")
    List<Prediction> autoCompletePlace(@Header("Authorization") String authorization,
                               @Query("input") String input, @Query("components") String components,
                               @Query("location") String location, @Query("radius") Long radius);

    @GET("/user_attributes/")
    UserAttributeSet getUserAttributes(@Header("Authorization") String authorization);

    @PUT("/user_attributes/")
    UserAttributeSet editUserAttributes(@Header("Authorization") String authorization,
                                      @Body UserAttributeSet userAttributes);

    @POST("/devices/")
    Device addDevice(@Header("Authorization") String authorization, @Body Device device);

    @PUT("/devices/{id}/")
    Device editDevice(@Path("id") long id, @Header("Authorization") String authorization, @Body Device device);

    @GET("/devices/{id}/")
    Device getDevice(@Path("id") long id, @Header("Authorization") String authorization);

    @DELETE("/devices/{id}/")
    JsonObject deleteDevice(@Path("id") long id, @Header("Authorization") String authorization);

    @GET("/friends/")
    Page<Friend> listFriends(@Header("Authorization") String authorization,
                               @Query("page") String page, @Query("page_size") String pageSize,
                               @Query("category") String category);

    @GET("/friends/")
    Page<Friend> listNonMemberFriends(@Header("Authorization") String authorization,
                             @Query("page") String page, @Query("page_size") String pageSize,
                             @Query("category") String category, @Query("exclude_event") long eventId);

    @POST("/friends/")
    Friend addFriend(@Header("Authorization") String authorization, @Body Friend friend);

    @PUT("/friends/{id}/")
    Plan editFriend(@Path("id") long id, @Header("Authorization") String authorization,
                    @Body Friend friend);

    @FormUrlEncoded
    @PUT("/friends/{id}/close/")
    Friend setCloseFriend(@Path("id") long id, @Header("Authorization") String authorization,
                        @Field("close") boolean close);

    @GET("/friends/search/")
    Page<Profile> searchProfiles(@Header("Authorization") String authorization,
                               @Query("page") String page, @Query("page_size") String pageSize,
                               @Query("search") String searchText);

    @POST("/plans/")
    Plan addPlan(@Header("Authorization") String authorization, @Body Plan plan);

    @GET("/plans/")
    Page<Plan> listPlans(@Header("Authorization") String authorization,
                         @Query("page") String page, @Query("page_size") String pageSize,
                         @Query("category") String category,
                         @Query("latitude") Double latitude,
                         @Query("longitude") Double longitude);

    @POST("/plans/search/")
    Page<Plan> searchPlans(@Header("Authorization") String authorization,
                           @Body PlanSearchFilter searchFilter);

    @GET("/plans/")
    Page<Plan> listUserPlans(@Header("Authorization") String authorization,
                               @Query("page") String page, @Query("page_size") String pageSize, long userId);

    @PUT("/plans/{id}/")
    Plan editPlan(@Path("id") long id, @Header("Authorization") String authorization,
                    @Body Plan plan);

    @GET("/plans/{id}/")
    Plan getPlan(@Header("Authorization") String authorization, @Path("id") long id);

    @Multipart
    @POST("/contacts/find/")
    List<Contact> findContacts(@Header("Authorization") String authorization,
                               @Part("emails") TypedFile emails);

    @FormUrlEncoded
    @POST("/contacts/import/")
    JsonObject importContacts(@Header("Authorization") String authorization,
                              @Field("users") String userIds);

    @GET("/history/")
    JsonObject getUserHistory(@Header("Authorization") String authorization,
                              @Query("page") String page, @Query("page_size") String pageSize,
                              @Query("user") long userId);

    /**
     * check if there is a profile using this display_name
     * @return
     */
    @GET("/check_name/")
    JSONObject checkName(@Query("client_id") String clientId,
                      @Query("client_secret") String clientSecret,
                      @Query("name") String displayName);

    @POST("/fit_history/")
    List<FitHistory> addFitHistory(@Header("Authorization") String authorization,
                                   @Body List<FitHistory> historyList);

    /**
     *
     * @param authorization
     * @param page
     * @param pageSize
     * @param userId
     * @param period
     * @param startDate First day of period (month day or year)
     * @return
     */
    @GET("/fit_history/")
    Page<FitHistory> getFitHistory(@Header("Authorization") String authorization,
                                   @Query("page") String page, @Query("page_size") String pageSize,
                                   @Query("user") long userId, @Query("period") String period,
                                   @Query("start_date") long startDate);

}
