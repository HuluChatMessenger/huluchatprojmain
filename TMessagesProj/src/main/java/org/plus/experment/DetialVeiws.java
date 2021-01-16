package org.plus.experment;

public class DetialVeiws {

//    public class BottomContactLayout extends FrameLayout{
//
//        int [] res = {R.drawable.menu_chats,R.drawable.menu_calls};
//
//
//        public BottomContactLayout(@NonNull Context context) {
//            super(context);
//
//
//            LinearLayout linearLayout = new LinearLayout(context);
//            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
//            linearLayout.setWeightSum(4);
//
//            // Drawable backgroundDrawable = Theme.createSelectorWithBackgroundDrawable(Theme.getColor(Theme.key_chats_actionBackground), Theme.getColor(Theme.key_chats_actionPressedBackground));
//
//
//
//            for (int a = 0; a < res.length; a++){
//                int finalA = a;
//                FrameLayout holder = new FrameLayout(context){
//                    @Override
//                    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
//                        if(finalA == 1){
//                            canvas.drawLine(0,0,0,getMeasuredHeight(),Theme.dividerPaint);
//                            canvas.drawLine(0,0,0,getMeasuredHeight(),Theme.dividerPaint);
//                        }
//                        return super.drawChild(canvas, child, drawingTime);
//                    }
//
//
//                };
//                Drawable drawable = ApplicationLoader.applicationContext.getResources().getDrawable(res[a]);
//                drawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextBlack), PorterDuff.Mode.MULTIPLY));
//                ImageView imageView = new ImageView(context);
//                imageView.setImageDrawable(drawable);
//                imageView.setScaleType(ImageView.ScaleType.CENTER);
//                holder.addView(imageView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
//                linearLayout.addView(holder,LayoutHelper.createLinear(0,LayoutHelper.MATCH_PARENT,1f));
//                holder.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
//
//
//                int finalA1 = a;
//                holder.setOnClickListener(new OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        if(finalA1 == 1){
//                            if(currentShop != null && currentShop.phone_contacts != null && !currentShop.phone_contacts.isEmpty()){
//                                String phone =   currentShop.phone_contacts.get(0).phonenumber;
//                                try {
//                                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+" + phone));
//                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                    getParentActivity().startActivityForResult(intent, 500);
//                                } catch (Exception e) {
//                                    FileLog.e(e);
//                                }
//                            }
//                        }else if(finalA1 == 0){
//                            Bundle args = new Bundle();
//                            args.putInt("user_id", UserConfig.getInstance(currentAccount).getClientUserId());
//                            presentFragment(new ChatActivity(args));
//                        }
//                    }
//                });
//            }
//
//
//            FrameLayout holder = new FrameLayout(context);
//            TextView simpleTextView = new TextView(context);
//            simpleTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
//            simpleTextView.setText("OFFER");
//            simpleTextView.setTypeface(AndroidUtilities.getTypeface("fonts/Roboto-Black.ttf"));
//            simpleTextView.setGravity(Gravity.CENTER);
//            simpleTextView.setTextSize(14);
//            simpleTextView.setTextColor(Theme.getColor(Theme.key_chats_actionIcon));
//            holder.setBackgroundColor(ShopUtils.getIntAlphaColor(Theme.getColor(Theme.key_chats_actionBackground),1f));
//
//
//            holder.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                    OfferBottomSheet offerBottomSheet = new OfferBottomSheet(ProductDetailFragment.this,"i will buy it at ");
//                    offerBottomSheet.setOfferBottomSheetDelegate((price, comment) -> {
//                        TLObject object =  getMessagesController().getUserOrChat(username);
//                        if(object instanceof TLRPC.User){
//                            TLRPC.User user = (TLRPC.User)object;
//                            TLRPC.TL_messages_sendMessage reqSend = new TLRPC.TL_messages_sendMessage();
//                            reqSend.message = "OFFER MADE \uD83D\uDC47\uD83D\uDC47 \n\n"
//                                    + "Title => "+  productFull.get("title") + "\n"
//                                    + "Item Price => "+  productFull.get("price") + "\n"
//                                    + "Offer Price =>  " + price + "\n"
//                                    + "Link  =>  "+ ShopUtils.getProductLink(chat_id,product_id) + "\n";
//
//
//                            reqSend.silent = false;
//                            reqSend.peer = getMessagesController().getInputPeer(user.id);
//                            reqSend.random_id = Utilities.random.nextLong();
//                            reqSend.no_webpage = true;
//                            ConnectionsManager.getInstance(currentAccount).sendRequest(reqSend, new RequestDelegate() {
//                                @Override
//                                public void run(TLObject response, TLRPC.TL_error error) {
//                                    if(error == null){
//                                        AndroidUtilities.runOnUIThread(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                if(response instanceof TLRPC.TL_updateShortSentMessage){
//                                                    final TLRPC.TL_updateShortSentMessage res = (TLRPC.TL_updateShortSentMessage) response;
//                                                    String messageLink = "tg://openmessage?user_id={user_id}&message_id=" + res.id;
//                                                    messageLink = messageLink.replace("{user_id}",user.id + "");
//                                                    createOffer(messageLink,comment,price);
//                                                }
//                                            }
//                                        });
//
//                                    }
//                                }
//                            });
//                        }else{
//                            getMessagesController().openByUserName(username,ProductDetailFragment.this,1);
//                        }
//
//
//                    });
//                    showDialog(offerBottomSheet);
//                }
//            });
//
//
//            holder.addView(simpleTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
//            linearLayout.addView(holder,LayoutHelper.createLinear(0,LayoutHelper.MATCH_PARENT,2f));
//
//
//
//            addView(linearLayout,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.MATCH_PARENT));
//        }
//
//        @Override
//        protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
//            canvas.drawLine(child.getMeasuredWidth(),0,child.getMeasuredWidth() + 2,child.getMeasuredHeight(),Theme.dividerPaint);
//            return super.drawChild(canvas, child, drawingTime);
//        }
//
//    }
//
//    private static class PriceCell extends FrameLayout {
//
//        private TextView titleView;
//
//        public PriceCell(Context context) {
//            super(context);
//
//            titleView = new TextView(getContext());
//            titleView.setLines(1);
//            titleView.setSingleLine(true);
//            titleView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
//            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
//            titleView.setTypeface(AndroidUtilities.getTypeface("fonts/Roboto-Black.ttf"));
//            titleView.setPadding(AndroidUtilities.dp(22), AndroidUtilities.dp(15), AndroidUtilities.dp(22), AndroidUtilities.dp(8));
//            titleView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
//            titleView.setGravity(Gravity.CENTER_VERTICAL);
//            addView(titleView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
//        }
//
////        @Override
////        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
////            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(60), MeasureSpec.EXACTLY));
////        }
//
//        public void setText(String text) {
//            titleView.setText(text);
//        }
//    }
//
//    private static class TitleCell extends FrameLayout {
//
//        private TextView titleView;
//
//        public TitleCell(Context context) {
//            super(context);
//
//            titleView = new TextView(getContext());
//            titleView.setLines(1);
//            titleView.setSingleLine(true);
//            titleView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
//            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
//            titleView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
//            titleView.setPadding(AndroidUtilities.dp(22), AndroidUtilities.dp(15), AndroidUtilities.dp(22), AndroidUtilities.dp(8));
//            titleView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
//            titleView.setGravity(Gravity.CENTER_VERTICAL);
//            addView(titleView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
//        }
//
//
//        public void setText(String text) {
//            titleView.setText(text);
//        }
//    }
//
//    private class ContactChangeCell extends FrameLayout {
//
//        protected int currentAccount = UserConfig.selectedAccount;
//
//        private SimpleTextView nameTextView;
//        private SimpleTextView statusTextView;
//        private SimpleTextView profilePhotoChanged;
//        private BackupImageView avatarImageView;
//        private ImageView newImageView;
//
//        public ImageView getNewImageView() {
//            return newImageView;
//        }
//
//        public void setName(String name){
//            nameTextView.setText(name);
//        }
//
//        public void setImage(Bitmap bitmap){
//            avatarImageView.setImageBitmap(bitmap);
//        }
//
//
//        public BackupImageView getAvatartIamgeView(){
//            return avatarImageView;
//        }
//
//        public void setUser(TLRPC.User user){
//            if (user == null) {
//                return;
//            }
//            TLRPC.FileLocation photo = null;
//            if (user.photo != null) {
//                photo = user.photo.photo_small;
//            }
//            AvatarDrawable avatarDrawable = new AvatarDrawable(user);
//            avatarDrawable.setColor(Theme.getColor(Theme.key_avatar_backgroundInProfileBlue));
//            avatarImageView.setImage(ImageLocation.getForUser(user,false), "50_50", avatarDrawable,user);
//        }
//
//        public void setChat(TLRPC.Chat chat){
//            if (chat == null) {
//                return;
//            }
//            TLRPC.FileLocation photo = null;
//            if (chat.photo != null) {
//                photo = chat.photo.photo_small;
//
//            }
//            AvatarDrawable avatarDrawable = new AvatarDrawable(chat);
//            avatarDrawable.setColor(Theme.getColor(Theme.key_avatar_backgroundInProfileBlue));
//            avatarImageView.setImage(ImageLocation.getForChat(chat,false), "50_50", avatarDrawable,chat);
//        }
//
//        public void setAbout(String about){
//            statusTextView.setText(about);
//        }
//
//        public BackupImageView getAvatarImageView() {
//            return avatarImageView;
//        }
//
//
//        public void setAvatarImageViewClickListener(final OnClickListener onClickListener) {
//            avatarImageView.setOnClickListener(v -> {
//                onClickListener.onClick(ContactChangeCell.this);
//            });
//        }
//
//        public ContactChangeCell(@NonNull Context context) {
//            super(context);
//            setWillNotDraw(false);
//
//            avatarImageView = new BackupImageView(context);
//            avatarImageView.setRoundRadius(AndroidUtilities.dp(24));
//            addView(avatarImageView, LayoutHelper.createFrame(50, 50, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, (LocaleController.isRTL ? 0 : 11), 11, (LocaleController.isRTL ? 11 : 0), 0));
//
////        final Activity activity = (Activity) context;
////        avatarImageView.setOnClickListener(v -> {
//////            openPic(user_id);
////            openAvatar();
////            Toast.makeText(context, "clicked", Toast.LENGTH_SHORT).show();
////        });
//
//            nameTextView = new SimpleTextView(context);
//            nameTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
//            nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
//            nameTextView.setTextSize(17);
//            nameTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP);
//            addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 20, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, (LocaleController.isRTL ? 0 : 72), 14, (LocaleController.isRTL ? 72 : 0), 0));
//
//            profilePhotoChanged = new SimpleTextView(context);
//            profilePhotoChanged.setTextColor( Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
//            profilePhotoChanged.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
//            profilePhotoChanged.setTextSize(14);
//            profilePhotoChanged.setText(LocaleController.getString("ProfilePhotoChanged", R.string.ProfilePhotoChanged));
//            profilePhotoChanged.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP);
//            addView(profilePhotoChanged, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 20, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, (LocaleController.isRTL ? 0 : 72), 40, (LocaleController.isRTL ? 72 : 0), 0));
//
//            statusTextView = new SimpleTextView(context);
//            statusTextView.setTextColor( Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
//            statusTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
//            statusTextView.setTextSize(14);
//            statusTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP);
//            addView(statusTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, (LocaleController.isRTL ? 0 : 72), 65, (LocaleController.isRTL ? 72 : 0), 0));
//
////        newImageView = new ImageView(context);
////        newImageView.setImageResource(R.drawable.appwall_new_2);
////        newImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
////        addView(newImageView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.TOP, (LocaleController.isRTL ? 15 : 0), 15, (LocaleController.isRTL ? 0 : 15), 0));
//        }
//
//        @Override
//        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(100), MeasureSpec.EXACTLY));
//        }
//
//        @Override
//        protected void onDraw(Canvas canvas) {
//            canvas.drawLine(AndroidUtilities.dp(LocaleController.isRTL ? 0 : 60), getHeight() - 1, getMeasuredWidth() - AndroidUtilities.dp(LocaleController.isRTL ? 60 : 0), getHeight() - 1, Theme.dividerPaint);
//
//        }
//
//    }
//
//    public class TableCell extends LinearLayout {
//
//        private BackupImageView imageView;
//        private TextView nameTextView;
//        private TextView usernameTextView;
//        private AvatarDrawable avatarDrawable;
//
//        public TableCell(Context context) {
//            super(context);
//
//            setOrientation(HORIZONTAL);
//
//            avatarDrawable = new AvatarDrawable();
//            avatarDrawable.setTextSize(AndroidUtilities.dp(12));
//
//            imageView = new BackupImageView(context);
//            imageView.setRoundRadius(AndroidUtilities.dp(14));
//            addView(imageView, LayoutHelper.createLinear(28, 28, 12, 4, 0, 0));
//
//            nameTextView = new TextView(context);
//            nameTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
//            nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
//            nameTextView.setSingleLine(true);
//            nameTextView.setGravity(Gravity.LEFT);
//            nameTextView.setEllipsize(TextUtils.TruncateAt.END);
//            addView(nameTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 12, 0, 0, 0));
//
//            usernameTextView = new TextView(context);
//            usernameTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
//            usernameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
//            usernameTextView.setSingleLine(true);
//            usernameTextView.setGravity(Gravity.LEFT);
//            usernameTextView.setEllipsize(TextUtils.TruncateAt.END);
//            addView(usernameTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 12, 0, 8, 0));
//        }
//
//        @Override
//        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(36), MeasureSpec.EXACTLY));
//        }
//
//        public void setData(String image,String key,String value){
//            if(ShopUtils.isEmpty(key) || ShopUtils.isEmpty(value)){
//                nameTextView.setText("");
//                usernameTextView.setText("");
//                imageView.setImageDrawable(null);
//                return;
//            }
//            avatarDrawable.setInfo(5,value,null);
//            avatarDrawable.setSmallSize(true);
//            nameTextView.setText(value);
//            usernameTextView.setText(key);
//            imageView.setImage(image, "50_50", avatarDrawable);
//
//        }
//
//        public void setUser(TLRPC.User user) {
//            if (user == null) {
//                nameTextView.setText("");
//                usernameTextView.setText("");
//                imageView.setImageDrawable(null);
//                return;
//            }
//            avatarDrawable.setInfo(user);
//            if (user.photo != null && user.photo.photo_small != null) {
//                imageView.setImage(ImageLocation.getForUser(user, false), "50_50", avatarDrawable, user);
//            } else {
//                imageView.setImageDrawable(avatarDrawable);
//            }
//            nameTextView.setText(UserObject.getUserName(user));
//            if (user.username != null) {
//                usernameTextView.setText("@" + user.username);
//            } else {
//                usernameTextView.setText("");
//            }
//            imageView.setVisibility(VISIBLE);
//            usernameTextView.setVisibility(VISIBLE);
//        }
//
//        public void setChat(TLRPC.Chat chat) {
//            if (chat == null) {
//                nameTextView.setText("");
//                usernameTextView.setText("");
//                imageView.setImageDrawable(null);
//                return;
//            }
//            avatarDrawable.setInfo(chat);
//            if (chat.photo != null && chat.photo.photo_small != null) {
//                imageView.setImage(ImageLocation.getForChat(chat, false), "50_50", avatarDrawable, chat);
//            } else {
//                imageView.setImageDrawable(avatarDrawable);
//            }
//            nameTextView.setText(chat.title);
//            if (chat.username != null) {
//                usernameTextView.setText("@" + chat.username);
//            } else {
//                usernameTextView.setText("");
//            }
//            imageView.setVisibility(VISIBLE);
//            usernameTextView.setVisibility(VISIBLE);
//        }
//
//        public void setText(String text) {
//            imageView.setVisibility(INVISIBLE);
//            usernameTextView.setVisibility(INVISIBLE);
//            nameTextView.setText(text);
//        }
//
//        @Override
//        public void invalidate() {
//            super.invalidate();
//            nameTextView.invalidate();
//        }
//
//
//
//        public void setIsDarkTheme(boolean isDarkTheme) {
//            if (isDarkTheme) {
//                nameTextView.setTextColor(0xffffffff);
//                usernameTextView.setTextColor(0xffbbbbbb);
//            } else {
//                nameTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
//                usernameTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
//            }
//        }
//    }
}
