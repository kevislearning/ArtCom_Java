package com.artgallery.config;

import com.artgallery.domain.*;
import com.artgallery.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IllustrationRepository illustrationRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private CommissionRepository commissionRepository;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Chỉ chạy seeder nếu chưa có user nào trong hệ thống
        if (userRepository.count() > 0) {
            System.out.println("[DatabaseSeeder] Database already contains data. Skipping seeding.");
            return;
        }

        System.out.println("[DatabaseSeeder] Seeding database...");

        // 1. Tạo Người dùng (Users & Artists)
        String defaultPasswordHash = passwordEncoder.encode("password123");

        User admin = User.builder()
                .username("admin")
                .email("admin@artcom.me")
                .passwordHash(defaultPasswordHash)
                .nickname("Administrator")
                .isArtist(true)
                .walletBalance(10000000.0) // 10 triệu VNĐ
                .bio("Art Gallery Developer & Admin Account.")
                .avatarUrl("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=200&h=200&fit=crop")
                .bannerUrl("https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=80&w=1200&h=400&fit=crop")
                .build();

        User vincent = User.builder()
                .username("vincent")
                .email("vincent@artcom.me")
                .passwordHash(defaultPasswordHash)
                .nickname("Vincent van Gogh")
                .isArtist(true)
                .walletBalance(3500000.0) // 3.5 triệu VNĐ
                .bio("Post-Impressionist painter. I seek, I strive, I am in it with all my heart.")
                .avatarUrl("https://images.unsplash.com/photo-1579783902614-a3fb3927b6a5?q=80&w=200&h=200&fit=crop")
                .bannerUrl("https://images.unsplash.com/photo-1541701494587-cb58502866ab?q=80&w=1200&h=400&fit=crop")
                .build();

        User monet = User.builder()
                .username("monet")
                .email("monet@artcom.me")
                .passwordHash(defaultPasswordHash)
                .nickname("Claude Monet")
                .isArtist(true)
                .walletBalance(0.0)
                .bio("Founder of French Impressionist painting. The richness I achieve comes from Nature.")
                .avatarUrl("https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=200&h=200&fit=crop")
                .bannerUrl("https://images.unsplash.com/photo-1447752875215-b2761acb3c5d?q=80&w=1200&h=400&fit=crop")
                .build();

        User mitsuki = User.builder()
                .username("mitsuki")
                .email("mitsuki@artcom.me")
                .passwordHash(defaultPasswordHash)
                .nickname("Mitsuki Anime Art")
                .isArtist(true)
                .walletBalance(1200000.0) // 1.2 triệu VNĐ
                .bio("Professional anime illustrator and manga artist. Open for fantasy and cute characters!")
                .avatarUrl("https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=200&h=200&fit=crop")
                .bannerUrl("https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?q=80&w=1200&h=400&fit=crop")
                .build();

        User skylar = User.builder()
                .username("skylar")
                .email("skylar@artcom.me")
                .passwordHash(defaultPasswordHash)
                .nickname("Skylar Digital")
                .isArtist(true)
                .walletBalance(2000000.0) // 2 triệu VNĐ
                .bio("Digital landscape artist. Futuristic cityscapes, sci-fi concept arts, and cyberpunk streets.")
                .avatarUrl("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=200&h=200&fit=crop")
                .bannerUrl("https://images.unsplash.com/photo-1515621061946-eff1c2a352bd?q=80&w=1200&h=400&fit=crop")
                .build();

        User alice = User.builder()
                .username("alice")
                .email("alice@artcom.me")
                .passwordHash(defaultPasswordHash)
                .nickname("Alice Smith")
                .isArtist(false)
                .walletBalance(5000000.0) // 5 triệu VNĐ
                .bio("Art lover and collector. Interested in classic oil paintings.")
                .avatarUrl("https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=200&h=200&fit=crop")
                .build();

        User bob = User.builder()
                .username("bob")
                .email("bob@artcom.me")
                .passwordHash(defaultPasswordHash)
                .nickname("Bob Jones")
                .isArtist(false)
                .walletBalance(500000.0) // 500k VNĐ
                .bio("Just browsing some fine arts around the community.")
                .avatarUrl("https://images.unsplash.com/photo-1500648767791-00dcc994a43e?q=80&w=200&h=200&fit=crop")
                .build();

        User emily = User.builder()
                .username("emily")
                .email("emily@artcom.me")
                .passwordHash(defaultPasswordHash)
                .nickname("Emily Watson")
                .isArtist(false)
                .walletBalance(1500000.0) // 1.5 triệu VNĐ
                .bio("Graphic design student. Fan of digital arts and anime style.")
                .avatarUrl("https://images.unsplash.com/photo-1438761681033-6461ffad8d80?q=80&w=200&h=200&fit=crop")
                .build();

        // Lưu người dùng vào CSDL trước
        userRepository.saveAll(Arrays.asList(admin, vincent, monet, mitsuki, skylar, alice, bob, emily));

        // 2. Tạo Tranh vẽ (Illustrations)
        Illustration i1_v = Illustration.builder()
                .artist(vincent)
                .title("The Starry Night")
                .description("Fascinating view of the night sky from my asylum window in Saint-Rémy-de-Provence.")
                .imageUrls(Collections.singletonList("https://images.unsplash.com/photo-1541701494587-cb58502866ab?q=80&w=800"))
                .tags(Arrays.asList("classic", "nature", "oilpainting", "starrysky"))
                .visibility("everyone")
                .commentsEnabled(true)
                .viewsCount(250)
                .likesCount(2)
                .bookmarksCount(1)
                .commentsCount(2)
                .build();

        Illustration i2_v = Illustration.builder()
                .artist(vincent)
                .title("Wheatfield with Crows")
                .description("A dramatic wind-swept wheat field under a dark, stormy sky populated by flying crows.")
                .imageUrls(Collections.singletonList("https://images.unsplash.com/photo-1579783928621-7a13d66a62d1?q=80&w=800"))
                .tags(Arrays.asList("classic", "nature", "oilpainting"))
                .visibility("logged_in")
                .commentsEnabled(true)
                .viewsCount(120)
                .build();

        Illustration i3_v = Illustration.builder()
                .artist(vincent)
                .title("My Private Studio Sketch")
                .description("A raw, charcoal draft sketch of my temporary workspace.")
                .imageUrls(Collections.singletonList("https://images.unsplash.com/photo-1580136579312-94651dfd596d?q=80&w=800"))
                .tags(Arrays.asList("classic", "sketch"))
                .visibility("private")
                .commentsEnabled(false)
                .viewsCount(5)
                .build();

        Illustration i1_m = Illustration.builder()
                .artist(monet)
                .title("Water Lilies")
                .description("Impression of the lily pond in my garden at Giverny. Exploring light and reflection.")
                .imageUrls(Collections.singletonList("https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?q=80&w=800"))
                .tags(Arrays.asList("classic", "nature", "impressionism"))
                .visibility("everyone")
                .commentsEnabled(true)
                .viewsCount(190)
                .likesCount(1)
                .build();

        Illustration i2_m = Illustration.builder()
                .artist(monet)
                .title("The Artist's Garden")
                .description("Paths of colorful irises and climbing roses leading up to the house.")
                .imageUrls(Collections.singletonList("https://images.unsplash.com/photo-1447752875215-b2761acb3c5d?q=80&w=800"))
                .tags(Arrays.asList("nature", "impressionism"))
                .visibility("everyone")
                .commentsEnabled(true)
                .viewsCount(80)
                .build();

        Illustration i1_mit = Illustration.builder()
                .artist(mitsuki)
                .title("Magical Forest Girl")
                .description("A bright pastel illustration of an elf girl surrounded by glowing mushrooms.")
                .imageUrls(Collections.singletonList("https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?q=80&w=800"))
                .tags(Arrays.asList("anime", "manga", "kawaii", "fantasy", "digitalart"))
                .visibility("everyone")
                .commentsEnabled(true)
                .viewsCount(320)
                .bookmarksCount(1)
                .commentsCount(1)
                .build();

        Illustration i2_mit = Illustration.builder()
                .artist(mitsuki)
                .title("Sky Above the Clouds")
                .description("Anime landscape scenery under golden hour sunlight. Inspired by Shinkai's films.")
                .imageUrls(Collections.singletonList("https://images.unsplash.com/photo-1578632767115-351597cf2477?q=80&w=800"))
                .tags(Arrays.asList("anime", "fantasy", "digitalart"))
                .visibility("everyone")
                .commentsEnabled(true)
                .viewsCount(140)
                .build();

        Illustration i3_mit = Illustration.builder()
                .artist(mitsuki)
                .title("Cyberpunk Cafe")
                .description("Neon-lit cafe shop featuring a cute barista. Exclusively for logged in users.")
                .imageUrls(Collections.singletonList("https://images.unsplash.com/photo-1528459801416-a9e53bbf4e17?q=80&w=800"))
                .tags(Arrays.asList("anime", "kawaii", "cyberpunk", "digitalart"))
                .visibility("logged_in")
                .commentsEnabled(true)
                .viewsCount(95)
                .build();

        Illustration i1_sky = Illustration.builder()
                .artist(skylar)
                .title("Neon Metropolis")
                .description("A vast, futuristic city skyline at midnight under pouring acid rain.")
                .imageUrls(Collections.singletonList("https://images.unsplash.com/photo-1515621061946-eff1c2a352bd?q=80&w=800"))
                .tags(Arrays.asList("scifi", "cyberpunk", "digital", "landscape"))
                .visibility("everyone")
                .commentsEnabled(true)
                .viewsCount(110)
                .build();

        Illustration i2_sky = Illustration.builder()
                .artist(skylar)
                .title("Abyss Expedition")
                .description("Concept art featuring space explorers discovering ancient relics in a deep canyon.")
                .imageUrls(Collections.singletonList("https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=80&w=800"))
                .tags(Arrays.asList("scifi", "digital", "conceptart"))
                .visibility("everyone")
                .commentsEnabled(true)
                .viewsCount(65)
                .build();

        // Lưu tranh vẽ vào CSDL
        illustrationRepository.saveAll(Arrays.asList(i1_v, i2_v, i3_v, i1_m, i2_m, i1_mit, i2_mit, i3_mit, i1_sky, i2_sky));

        // Cập nhật thống kê tạm thời cho nghệ sĩ
        vincent.setTotalViews(375);
        vincent.setTotalLikes(2);
        vincent.setTotalBookmarks(1);
        vincent.setTotalComments(2);
        userRepository.save(vincent);

        monet.setTotalViews(270);
        monet.setTotalLikes(1);
        userRepository.save(monet);

        mitsuki.setTotalViews(555);
        mitsuki.setTotalBookmarks(1);
        mitsuki.setTotalComments(1);
        userRepository.save(mitsuki);

        skylar.setTotalViews(175);
        userRepository.save(skylar);

        // 3. Tạo tương tác Like & Bookmark
        Like l1 = Like.builder().user(alice).illustration(i1_v).build();
        Like l2 = Like.builder().user(bob).illustration(i1_v).build();
        Like l3 = Like.builder().user(bob).illustration(i1_m).build();
        likeRepository.saveAll(Arrays.asList(l1, l2, l3));

        Bookmark b1 = Bookmark.builder().user(alice).illustration(i1_v).build();
        Bookmark b2 = Bookmark.builder().user(emily).illustration(i1_mit).build();
        bookmarkRepository.saveAll(Arrays.asList(b1, b2));

        // 4. Tạo quan hệ Theo dõi (Follows)
        Follow f1 = Follow.builder().follower(alice).following(vincent).build();
        Follow f2 = Follow.builder().follower(alice).following(monet).build();
        Follow f3 = Follow.builder().follower(bob).following(vincent).build();
        Follow f4 = Follow.builder().follower(emily).following(mitsuki).build();
        Follow f5 = Follow.builder().follower(emily).following(skylar).build();
        followRepository.saveAll(Arrays.asList(f1, f2, f3, f4, f5));

        // 5. Tạo Bình luận (Comments)
        Comment c1 = Comment.builder()
                .user(alice)
                .illustration(i1_v)
                .content("This is absolutely breathtaking, Vincent! The stars feel so alive.")
                .build();
        commentRepository.save(c1);

        Comment c1_reply = Comment.builder()
                .user(vincent)
                .illustration(i1_v)
                .parentComment(c1)
                .content("Thank you, Alice! I painted it with the hope of catching the night's magic.")
                .build();
        commentRepository.save(c1_reply);

        Comment c2 = Comment.builder()
                .user(bob)
                .illustration(i1_v)
                .content("Classic masterpiece! Love the swirling wind.")
                .build();
        commentRepository.save(c2);

        Comment c3 = Comment.builder()
                .user(emily)
                .illustration(i1_mit)
                .content("The colors are so vibrant and cute! Love the style.")
                .build();
        commentRepository.save(c3);

        // 6. Tạo Yêu cầu vẽ tranh (Commissions)
        // Commission 1: Alice -> Vincent (Pending)
        Commission comm1 = Commission.builder()
                .client(alice)
                .artist(vincent)
                .title("Family Portrait in Classic Style")
                .description("I would love to have a classic portrait of my family of four, rendered in your signature impasto swirling brush strokes.")
                .price(1500000.0) // 1.5 triệu VNĐ
                .deadline(new Date(System.currentTimeMillis() + 14L * 24 * 60 * 60 * 1000)) // 14 ngày sau
                .status("pending")
                .paymentStatus("unpaid")
                .isPrivate(false)
                .build();

        // Commission 2: Bob -> Monet (In Progress - Escrow)
        Commission comm2 = Commission.builder()
                .client(bob)
                .artist(monet)
                .title("Lotus Pond Painting for Living Room")
                .description("Need a wide lotus pond painting capturing the morning mist. Around 1.2m x 0.8m size.")
                .price(2000000.0) // 2 triệu VNĐ
                .deadline(new Date(System.currentTimeMillis() + 10L * 24 * 60 * 60 * 1000))
                .status("in_progress")
                .paymentStatus("escrow")
                .isPrivate(false)
                .build();

        // Commission 3: Admin -> Vincent (Completed - Paid)
        Commission comm3 = Commission.builder()
                .client(admin)
                .artist(vincent)
                .title("Developer Server Room Impression")
                .description("An impressionist interpretation of server racks, flashing green lights, and blue wires.")
                .price(3000000.0) // 3 triệu VNĐ
                .deadline(new Date(System.currentTimeMillis() - 2L * 24 * 60 * 60 * 1000)) // Hạn chót đã qua 2 ngày
                .status("completed")
                .paymentStatus("paid_to_artist")
                .resultIllustration(i2_v) // gán kết quả là Wheatfield
                .isPrivate(false)
                .build();

        commissionRepository.saveAll(Arrays.asList(comm1, comm2, comm3));

        // 7. Tạo Lịch sử giao dịch ví (Wallet Transactions)
        WalletTransaction t1 = WalletTransaction.builder()
                .user(alice)
                .amount(5000000.0)
                .type("deposit")
                .description("Nap tien qua QR Ngan hang")
                .build();

        WalletTransaction t2 = WalletTransaction.builder()
                .user(bob)
                .amount(2500000.0)
                .type("deposit")
                .description("Nap tien qua Vi MoMo")
                .build();

        WalletTransaction t2_escrow = WalletTransaction.builder()
                .user(bob)
                .amount(-2000000.0)
                .type("escrow_hold")
                .reference(comm2)
                .description("Tam khoa cọc dat tranh: Lotus Pond Painting")
                .build();

        WalletTransaction t3 = WalletTransaction.builder()
                .user(admin)
                .amount(13000000.0)
                .type("deposit")
                .description("Nap tien khoi tao he thong")
                .build();

        WalletTransaction t3_hold = WalletTransaction.builder()
                .user(admin)
                .amount(-3000000.0)
                .type("escrow_hold")
                .reference(comm3)
                .description("Tam khoa coc ve tranh phong Server")
                .build();

        WalletTransaction t3_release_admin = WalletTransaction.builder()
                .user(admin)
                .amount(3000000.0)
                .type("escrow_release")
                .reference(comm3)
                .description("Giai ngan thanh toan cho họa si")
                .build();

        WalletTransaction t3_release_artist = WalletTransaction.builder()
                .user(vincent)
                .amount(3000000.0)
                .type("escrow_release")
                .reference(comm3)
                .description("Nhan tien ve tranh phong Server tu Admin")
                .build();

        WalletTransaction t4_vincent = WalletTransaction.builder()
                .user(vincent)
                .amount(500000.0)
                .type("deposit")
                .description("Nap tien vi nap thu nghiem")
                .build();

        walletTransactionRepository.saveAll(Arrays.asList(t1, t2, t2_escrow, t3, t3_hold, t3_release_admin, t3_release_artist, t4_vincent));

        // 8. Tạo Lịch sử Chat (Messages)
        Message m1 = Message.builder()
                .sender(alice)
                .receiver(vincent)
                .content("Hi Vincent! I really love your starry sky paintings. Are you open for a commission?")
                .isRead(true)
                .build();

        Message m2 = Message.builder()
                .sender(vincent)
                .receiver(alice)
                .content("Hello Alice! Yes, I am. What kind of subject would you like me to paint?")
                .isRead(true)
                .build();

        Message m3 = Message.builder()
                .sender(alice)
                .receiver(vincent)
                .content("I would like a portrait of my family, but with your swirling starry sky in the background.")
                .isRead(false)
                .build();

        messageRepository.saveAll(Arrays.asList(m1, m2, m3));

        // 9. Tạo Thông báo (Notifications)
        Notification n1 = Notification.builder()
                .recipient(vincent)
                .actor(alice)
                .type("follow")
                .contentPreview("Alice Smith has started following you")
                .isRead(false)
                .build();

        Notification n2 = Notification.builder()
                .recipient(vincent)
                .actor(alice)
                .type("like")
                .targetId(i1_v.getId())
                .targetModel("Illustration")
                .contentPreview("Alice Smith liked your artwork 'The Starry Night'")
                .isRead(false)
                .build();

        Notification n3 = Notification.builder()
                .recipient(vincent)
                .actor(alice)
                .type("commission_update")
                .targetId(comm1.getId())
                .targetModel("Commission")
                .contentPreview("You have a new commission request from Alice Smith")
                .isRead(false)
                .build();

        Notification n4 = Notification.builder()
                .recipient(monet)
                .actor(alice)
                .type("follow")
                .contentPreview("Alice Smith has started following you")
                .isRead(true)
                .build();

        notificationRepository.saveAll(Arrays.asList(n1, n2, n3, n4));

        System.out.println("[DatabaseSeeder] Database seeding completed successfully!");
    }
}
