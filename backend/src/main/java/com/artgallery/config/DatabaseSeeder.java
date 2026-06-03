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

    // Pools of Unsplash High-Quality URLs
    private static final String[] AVATAR_POOL = {
        "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=200&h=200&fit=crop",
        "https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=200&h=200&fit=crop",
        "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?q=80&w=200&h=200&fit=crop",
        "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?q=80&w=200&h=200&fit=crop",
        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=200&h=200&fit=crop",
        "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?q=80&w=200&h=200&fit=crop",
        "https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=200&h=200&fit=crop",
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=200&h=200&fit=crop",
        "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?q=80&w=200&h=200&fit=crop",
        "https://images.unsplash.com/photo-1517841905240-472988babdf9?q=80&w=200&h=200&fit=crop",
        "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?q=80&w=200&h=200&fit=crop",
        "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?q=80&w=200&h=200&fit=crop",
        "https://images.unsplash.com/photo-1534308983496-4fabb1a015ee?q=80&w=200&h=200&fit=crop",
        "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?q=80&w=200&h=200&fit=crop",
        "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?q=80&w=200&h=200&fit=crop",
        "https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?q=80&w=200&h=200&fit=crop",
        "https://images.unsplash.com/photo-1542206395-9feb3edaa68d?q=80&w=200&h=200&fit=crop",
        "https://images.unsplash.com/photo-1501196354995-cbb51c65aaea?q=80&w=200&h=200&fit=crop",
        "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?q=80&w=200&h=200&fit=crop",
        "https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?q=80&w=200&h=200&fit=crop"
    };

    private static final String[] BANNER_POOL = {
        "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=80&w=1200&h=400&fit=crop",
        "https://images.unsplash.com/photo-1541701494587-cb58502866ab?q=80&w=1200&h=400&fit=crop",
        "https://images.unsplash.com/photo-1447752875215-b2761acb3c5d?q=80&w=1200&h=400&fit=crop",
        "https://images.unsplash.com/photo-1515621061946-eff1c2a352bd?q=80&w=1200&h=400&fit=crop",
        "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?q=80&w=1200&h=400&fit=crop",
        "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?q=80&w=1200&h=400&fit=crop",
        "https://images.unsplash.com/photo-1579783928621-7a13d66a62d1?q=80&w=1200&h=400&fit=crop",
        "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?q=80&w=1200&h=400&fit=crop",
        "https://images.unsplash.com/photo-1501854140801-50d01698950b?q=80&w=1200&h=400&fit=crop",
        "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?q=80&w=1200&h=400&fit=crop"
    };

    private static final String[] ARTWORK_POOL = {
        "https://images.unsplash.com/photo-1579783902614-a3fb3927b6a5?q=80&w=800",
        "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?q=80&w=800",
        "https://images.unsplash.com/photo-1580136579312-94651dfd596d?q=80&w=800",
        "https://images.unsplash.com/photo-1578632767115-351597cf2477?q=80&w=800",
        "https://images.unsplash.com/photo-1528459801416-a9e53bbf4e17?q=80&w=800",
        "https://images.unsplash.com/photo-1605721911519-3dfeb3be25e7?q=80&w=800",
        "https://images.unsplash.com/photo-1549887534-1541e9326642?q=80&w=800",
        "https://images.unsplash.com/photo-1561214115-f2f134cc4912?q=80&w=800",
        "https://images.unsplash.com/photo-1579783928621-7a13d66a62d1?q=80&w=800",
        "https://images.unsplash.com/photo-1541701494587-cb58502866ab?q=80&w=800",
        "https://images.unsplash.com/photo-1447752875215-b2761acb3c5d?q=80&w=800",
        "https://images.unsplash.com/photo-1515621061946-eff1c2a352bd?q=80&w=800",
        "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=80&w=800",
        "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?q=80&w=800",
        "https://images.unsplash.com/photo-1547891654-e66ed7edd96c?q=80&w=800",
        "https://images.unsplash.com/photo-1513364776144-60967b0f800f?q=80&w=800",
        "https://images.unsplash.com/photo-1536924940846-227afb31e2a5?q=80&w=800",
        "https://images.unsplash.com/photo-1460661419201-fd4cecdf8a8b?q=80&w=800",
        "https://images.unsplash.com/photo-1501472312651-726afd116ff1?q=80&w=800",
        "https://images.unsplash.com/photo-1543857778-c4a1a3e0b2eb?q=80&w=800",
        "https://images.unsplash.com/photo-1541963463532-d68292c34b19?q=80&w=800",
        "https://images.unsplash.com/photo-1579783928586-78f9f8c65f9a?q=80&w=800",
        "https://images.unsplash.com/photo-1582562124811-c09040d0a901?q=80&w=800",
        "https://images.unsplash.com/photo-1582201942988-13e60e4556ee?q=80&w=800",
        "https://images.unsplash.com/photo-1576016770956-debb63d90029?q=80&w=800",
        "https://images.unsplash.com/photo-1594224056279-d101eb692631?q=80&w=800",
        "https://images.unsplash.com/photo-1558591710-4b4a1ae0f04d?q=80&w=800",
        "https://images.unsplash.com/photo-1518895949257-7621c3c786d7?q=80&w=800",
        "https://images.unsplash.com/photo-1554188718-d8af7b9d4e82?q=80&w=800",
        "https://images.unsplash.com/photo-1551913902-c92207136625?q=80&w=800"
    };

    private static final String[] ART_TITLES = {
        "Mystic River Scenery", "Golden Hour Echoes", "Neon Street Dreams", "A Quiet Afternoon In Kyoto",
        "Guardians of the Wild", "Spirits of Autumn Leaves", "Mechanical Awakening", "Enchanted Library Room",
        "Sailing the Cosmos", "Stargazing at Midnight Cliffs", "Crimson Sunset reflection", "Whisper of the Wind",
        "Cyber Cafe Barista", "Lost in Virtual Reality", "Deep Sea Exploration", "Ancient Relics Found",
        "Watercolor Garden Paths", "Winter Silence over Alps", "City Under Acid Rain", "Gateway to Shambhala"
    };

    private static final String[] ART_DESCRIPTIONS = {
        "A piece exploring contrast of warm and cold color palettes.",
        "Created after a peaceful walk in the countryside during golden hour.",
        "Futuristic neon-lit street concept art.",
        "An impression of water reflections on a beautiful pond.",
        "Fantasy scene capturing the mood of a mysterious forest.",
        "Character illustration showcasing modern streetwear fashion.",
        "Sketch study focusing on anatomy and dramatic lighting.",
        "Digital painting experiment done in Clip Studio Paint.",
        "Sci-fi spaceship hovering over a distant red dwarf planet.",
        "Classic oil painting reproduction using modern digital brushes."
    };

    private static final String[] TAGS_POOL = {
        "digitalart", "classic", "anime", "scifi", "landscape", "oilpainting",
        "fantasy", "cyberpunk", "conceptart", "nature", "kawaii", "sketch", "watercolor"
    };

    private static final String[] COMMENT_TEXTS = {
        "Bức tranh tuyệt đẹp! Màu sắc phối quá đỉnh.",
        "This is absolutely breathtaking! The lighting feels so alive.",
        "Mình rất thích nét vẽ này, nhìn nhẹ nhàng mà có chiều sâu ghê.",
        "Great composition and values! Keep up the amazing work.",
        "Wow, visual chất lượng thật sự. Nhìn lôi cuốn quá!",
        "Stunning detail! How long did it take you to paint this?",
        "Gam màu ấm áp làm người xem thấy rất bình yên.",
        "This reminds me of classic Ghibli backgrounds. Masterpiece!"
    };

    private static final String[] REPLY_TEXTS = {
        "Cảm ơn bạn rất nhiều nha! Mình vẽ mất tầm 6 tiếng.",
        "Thank you so much! Really glad you liked the lighting.",
        "Hihi cảm ơn bạn đã ủng hộ nét vẽ của mình nhé!",
        "Thanks! I appreciate the feedback on the composition.",
        "Cảm ơn bạn, mình có quay lại speedpaint đăng trên kênh cá nhân đó."
    };

    private static final String[] MESSAGE_TEXTS = {
        "Chào họa sĩ, mình rất thích tranh của bạn. Bạn còn nhận vẽ commission không?",
        "Chào bạn! Cảm ơn bạn đã quan tâm. Mình vẫn đang mở commission nhé.",
        "Tuyệt quá! Mình muốn vẽ một ảnh phong cảnh cyberpunk làm hình nền máy tính.",
        "Được nhé bạn ơi, yêu cầu này mình nhận vẽ khoảng 2 triệu VNĐ trong 10 ngày.",
        "Giá đó hợp lý quá, mình sẽ đặt cọc ngay trên web nhé. Cảm ơn họa sĩ!"
    };

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            System.out.println("[DatabaseSeeder] Database already contains data. Skipping seeding.");
            return;
        }

        System.out.println("[DatabaseSeeder] Seeding database with expanded dynamic dataset...");

        Random random = new Random();
        String defaultPasswordHash = passwordEncoder.encode("password123");

        // --- 1. CREATE CORE STATIC USERS ---
        User admin = User.builder()
                .username("admin")
                .email("admin@artcom.me")
                .passwordHash(defaultPasswordHash)
                .nickname("Administrator")
                .isArtist(true)
                .walletBalance(10000000.0)
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
                .walletBalance(3500000.0)
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
                .walletBalance(1200000.0)
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
                .walletBalance(2000000.0)
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
                .walletBalance(5000000.0)
                .bio("Art lover and collector. Interested in classic oil paintings.")
                .avatarUrl("https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=200&h=200&fit=crop")
                .build();

        User bob = User.builder()
                .username("bob")
                .email("bob@artcom.me")
                .passwordHash(defaultPasswordHash)
                .nickname("Bob Jones")
                .isArtist(false)
                .walletBalance(500000.0)
                .bio("Just browsing some fine arts around the community.")
                .avatarUrl("https://images.unsplash.com/photo-1500648767791-00dcc994a43e?q=80&w=200&h=200&fit=crop")
                .build();

        User emily = User.builder()
                .username("emily")
                .email("emily@artcom.me")
                .passwordHash(defaultPasswordHash)
                .nickname("Emily Watson")
                .isArtist(false)
                .walletBalance(1500000.0)
                .bio("Graphic design student. Fan of digital arts and anime style.")
                .avatarUrl("https://images.unsplash.com/photo-1438761681033-6461ffad8d80?q=80&w=200&h=200&fit=crop")
                .build();

        List<User> staticUsers = Arrays.asList(admin, vincent, monet, mitsuki, skylar, alice, bob, emily);
        userRepository.saveAll(staticUsers);

        // Lists to partition users for relation wiring
        List<User> allArtists = new ArrayList<>(Arrays.asList(vincent, monet, mitsuki, skylar));
        List<User> allClients = new ArrayList<>(Arrays.asList(admin, alice, bob, emily));
        List<User> allUsers = new ArrayList<>(staticUsers);

        // --- 2. GENERATE DYNAMIC USERS (20 Users) ---
        String[] dynamicArtistNames = {
            "Pierre-Auguste Renoir", "Edgar Degas", "Gustav Klimt", "Leonardo da Vinci", "Rembrandt van Rijn",
            "Kusanagi Studio", "Shiro Anime Art", "CyberVibe Designs", "NeonDreams SciFi", "InkSplash Watercolor"
        };
        String[] dynamicArtistUsernames = {
            "renoir", "degas", "klimt", "da_vinci", "rembrandt", "kusanagi", "shiro", "cyber_vibe", "neon_dreams", "ink_splash"
        };
        String[] dynamicArtistBios = {
            "Impressionist painter focused on beauty, femininity and light.",
            "Famous for my paintings of dancers. Art is not what you see, but what you make others see.",
            "Symbolist painter. All art is erotic.",
            "Renaissance man. Simplicity is the ultimate sophistication.",
            "Dutch Golden Age master of light, shadow, and human emotion.",
            "Traditional hand-painted anime background designers.",
            "Hobbyist anime and VTuber model designer. Cute characters are my life!",
            "Designing dark futuristic cityscapes and cyberpunk elements.",
            "Concept artist drawing giant robots, mechas and deep star spaces.",
            "Watercolor lover capturing realistic flower beds and wild birds."
        };

        String[] dynamicClientNames = {
            "Charlie Brown", "David Miller", "Fiona Green", "Grace Hopper", "Henry Ford",
            "Iris West", "Jack Sparrow", "Karen Gillan", "Leo Messi", "Mia Wallace"
        };
        String[] dynamicClientUsernames = {
            "charlie", "david", "fiona", "grace", "henry", "iris", "jack", "karen", "leo", "mia"
        };

        // Create Dynamic Artists
        for (int i = 0; i < dynamicArtistNames.length; i++) {
            User dynamicArtist = User.builder()
                    .username(dynamicArtistUsernames[i])
                    .email(dynamicArtistUsernames[i] + "@artcom.me")
                    .passwordHash(defaultPasswordHash)
                    .nickname(dynamicArtistNames[i])
                    .isArtist(true)
                    .walletBalance(500000.0 + random.nextInt(5) * 1000000.0)
                    .bio(dynamicArtistBios[i])
                    .avatarUrl(AVATAR_POOL[i])
                    .bannerUrl(BANNER_POOL[i % BANNER_POOL.length])
                    .build();
            userRepository.save(dynamicArtist);
            allArtists.add(dynamicArtist);
            allUsers.add(dynamicArtist);
        }

        // Create Dynamic Clients
        for (int i = 0; i < dynamicClientNames.length; i++) {
            User dynamicClient = User.builder()
                    .username(dynamicClientUsernames[i])
                    .email(dynamicClientUsernames[i] + "@artcom.me")
                    .passwordHash(defaultPasswordHash)
                    .nickname(dynamicClientNames[i])
                    .isArtist(false)
                    .walletBalance(1000000.0 + random.nextInt(10) * 1000000.0)
                    .bio("Interested in discovering beautiful arts. Professional art collector.")
                    .avatarUrl(AVATAR_POOL[10 + i])
                    .build();
            userRepository.save(dynamicClient);
            allClients.add(dynamicClient);
            allUsers.add(dynamicClient);
        }

        // --- 3. GENERATE ILLUSTRATIONS ---
        List<Illustration> allIllustrations = new ArrayList<>();

        // Generate core static illustrations first (Vincent & Monet & Mitsuki & Skylar originals)
        Illustration i1_v = Illustration.builder()
                .artist(vincent)
                .title("The Starry Night")
                .description("Fascinating view of the night sky from my asylum window in Saint-Rémy-de-Provence.")
                .imageUrls(Collections.singletonList("https://images.unsplash.com/photo-1541701494587-cb58502866ab?q=80&w=800"))
                .tags(Arrays.asList("classic", "nature", "oilpainting", "starrysky"))
                .visibility("everyone")
                .commentsEnabled(true)
                .viewsCount(250)
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

        List<Illustration> staticIllustrations = Arrays.asList(i1_v, i2_v, i3_v, i1_m, i2_m, i1_mit, i2_mit, i3_mit, i1_sky, i2_sky);
        illustrationRepository.saveAll(staticIllustrations);
        allIllustrations.addAll(staticIllustrations);

        // Dynamically generate illustrations for all artists (each artist gets 3 to 5 works)
        int artIndex = 0;
        for (User artist : allArtists) {
            // Already generated static ones for Vincent, Monet, Mitsuki, Skylar. Let's add more or seed others
            int countToSeed = 3 + random.nextInt(3); // 3 to 5 illustrations
            for (int j = 0; j < countToSeed; j++) {
                String imageUrl = ARTWORK_POOL[artIndex % ARTWORK_POOL.length];
                artIndex++;

                // Title and Description selection
                String title = ART_TITLES[random.nextInt(ART_TITLES.length)] + " (" + (j + 1) + ")";
                String description = ART_DESCRIPTIONS[random.nextInt(ART_DESCRIPTIONS.length)];

                // Gather tags
                List<String> tags = new ArrayList<>();
                int tagCount = 2 + random.nextInt(3);
                while (tags.size() < tagCount) {
                    String candidateTag = TAGS_POOL[random.nextInt(TAGS_POOL.length)];
                    if (!tags.contains(candidateTag)) {
                        tags.add(candidateTag);
                    }
                }

                // Visibility distribution: 80% everyone, 15% logged_in, 5% private
                int visChance = random.nextInt(100);
                String visibility = "everyone";
                if (visChance >= 80 && visChance < 95) {
                    visibility = "logged_in";
                } else if (visChance >= 95) {
                    visibility = "private";
                }

                Illustration ill = Illustration.builder()
                        .artist(artist)
                        .title(title)
                        .description(description)
                        .imageUrls(Collections.singletonList(imageUrl))
                        .tags(tags)
                        .visibility(visibility)
                        .commentsEnabled(random.nextInt(10) < 9) // 90% enabled
                        .viewsCount(20 + random.nextInt(480))
                        .build();

                illustrationRepository.save(ill);
                allIllustrations.add(ill);
            }
        }

        // --- 4. GENERATE INTERACTIVE FOLLOWS (All users follow 3-7 artists) ---
        List<Follow> allFollows = new ArrayList<>();
        for (User follower : allUsers) {
            int followsCount = 3 + random.nextInt(5); // 3 to 7 follows
            Set<UUID> followedIds = new HashSet<>();
            while (followedIds.size() < followsCount) {
                User following = allArtists.get(random.nextInt(allArtists.size()));
                if (!following.getId().equals(follower.getId()) && !followedIds.contains(following.getId())) {
                    followedIds.add(following.getId());
                    Follow follow = Follow.builder()
                            .follower(follower)
                            .following(following)
                            .build();
                    allFollows.add(follow);
                }
            }
        }
        followRepository.saveAll(allFollows);

        // --- 5. GENERATE INTERACTIVE LIKES & BOOKMARKS ---
        List<Like> allLikes = new ArrayList<>();
        List<Bookmark> allBookmarks = new ArrayList<>();

        for (Illustration ill : allIllustrations) {
            if ("private".equals(ill.getVisibility())) continue;

            // Generate Likes (15% to 60% of all users like this work)
            int likeTargetPercent = 15 + random.nextInt(46);
            int likeTargetCount = (allUsers.size() * likeTargetPercent) / 100;
            List<User> shuffledUsers = new ArrayList<>(allUsers);
            Collections.shuffle(shuffledUsers);

            int likesAdded = 0;
            for (int i = 0; i < likeTargetCount; i++) {
                User liker = shuffledUsers.get(i);
                Like like = Like.builder()
                        .user(liker)
                        .illustration(ill)
                        .build();
                allLikes.add(like);
                likesAdded++;
            }
            ill.setLikesCount(likesAdded);

            // Generate Bookmarks (5% to 25% of all users bookmark this work)
            int bookmarkTargetPercent = 5 + random.nextInt(21);
            int bookmarkTargetCount = (allUsers.size() * bookmarkTargetPercent) / 100;
            Collections.shuffle(shuffledUsers);

            int bookmarksAdded = 0;
            for (int i = 0; i < bookmarkTargetCount; i++) {
                User bookmarker = shuffledUsers.get(i);
                Bookmark bookmark = Bookmark.builder()
                        .user(bookmarker)
                        .illustration(ill)
                        .build();
                allBookmarks.add(bookmark);
                bookmarksAdded++;
            }
            ill.setBookmarksCount(bookmarksAdded);
            illustrationRepository.save(ill);
        }
        likeRepository.saveAll(allLikes);
        bookmarkRepository.saveAll(allBookmarks);

        // --- 6. GENERATE INTERACTIVE COMMENTS ---
        List<Comment> allComments = new ArrayList<>();
        for (Illustration ill : allIllustrations) {
            if (!ill.isCommentsEnabled() || "private".equals(ill.getVisibility())) continue;

            int commentCount = 1 + random.nextInt(5); // 1 to 5 comments
            int commentsAdded = 0;

            for (int c = 0; c < commentCount; c++) {
                User commentator = allUsers.get(random.nextInt(allUsers.size()));
                String content = COMMENT_TEXTS[random.nextInt(COMMENT_TEXTS.length)];

                Comment comment = Comment.builder()
                        .user(commentator)
                        .illustration(ill)
                        .content(content)
                        .build();
                commentRepository.save(comment);
                allComments.add(comment);
                commentsAdded++;

                // 30% chance for a reply
                if (random.nextInt(10) < 3) {
                    User replier = random.nextBoolean() ? ill.getArtist() : allUsers.get(random.nextInt(allUsers.size()));
                    String replyContent = REPLY_TEXTS[random.nextInt(REPLY_TEXTS.length)];
                    Comment reply = Comment.builder()
                            .user(replier)
                            .illustration(ill)
                            .parentComment(comment)
                            .content(replyContent)
                            .build();
                    commentRepository.save(reply);
                    allComments.add(reply);
                    commentsAdded++;
                }
            }
            ill.setCommentsCount(commentsAdded);
            illustrationRepository.save(ill);
        }

        // --- 7. GENERATE DYNAMIC COMMISSIONS ---
        List<Commission> allCommissions = new ArrayList<>();
        List<WalletTransaction> allTransactions = new ArrayList<>();

        String[] commissionStatuses = {"pending", "accepted", "in_progress", "completed", "canceled", "rejected"};
        int commissionsCount = 12 + random.nextInt(5); // 12 to 16 commissions

        for (int i = 0; i < commissionsCount; i++) {
            User client = allClients.get(random.nextInt(allClients.size()));
            User artist = allArtists.get(random.nextInt(allArtists.size()));

            if (client.getId().equals(artist.getId())) continue;

            double price = 1000000.0 + random.nextInt(5) * 500000.0; // 1M to 3M
            String status = commissionStatuses[random.nextInt(commissionStatuses.length)];
            String paymentStatus = "unpaid";

            if ("in_progress".equals(status) || "accepted".equals(status)) {
                paymentStatus = "escrow";
            } else if ("completed".equals(status)) {
                paymentStatus = "paid_to_artist";
            } else if ("canceled".equals(status) || "rejected".equals(status)) {
                paymentStatus = "refunded";
            }

            // Target illustration for completed commission
            Illustration resultArtwork = null;
            if ("completed".equals(status)) {
                // Find or build completed work illustration
                resultArtwork = Illustration.builder()
                        .artist(artist)
                        .title("[Commission Result] Artwork for " + client.getNickname())
                        .description("Delivered work based on request instructions.")
                        .imageUrls(Collections.singletonList(ARTWORK_POOL[random.nextInt(ARTWORK_POOL.length)]))
                        .tags(Arrays.asList("commission", "delivery"))
                        .visibility("everyone")
                        .commentsEnabled(true)
                        .viewsCount(20 + random.nextInt(150))
                        .build();
                illustrationRepository.save(resultArtwork);
                allIllustrations.add(resultArtwork);
            }

            Commission commission = Commission.builder()
                    .client(client)
                    .artist(artist)
                    .title("Custom artwork request #" + (i + 1))
                    .description("Detail description of the character or concept that I would like the artist to illustrate.")
                    .price(price)
                    .deadline(new Date(System.currentTimeMillis() + (7L + random.nextInt(14)) * 24 * 60 * 60 * 1000))
                    .status(status)
                    .paymentStatus(paymentStatus)
                    .isPrivate(random.nextBoolean())
                    .resultIllustration(resultArtwork)
                    .build();

            commissionRepository.save(commission);
            allCommissions.add(commission);

            // Create transactions matching payment status
            if ("escrow".equals(paymentStatus)) {
                WalletTransaction escrowHold = WalletTransaction.builder()
                        .user(client)
                        .amount(-price)
                        .type("escrow_hold")
                        .reference(commission)
                        .description("Tạm khóa cọc commission #" + commission.getId())
                        .build();
                allTransactions.add(escrowHold);
            } else if ("paid_to_artist".equals(paymentStatus)) {
                // Client deposits
                WalletTransaction clientDeposit = WalletTransaction.builder()
                        .user(client)
                        .amount(price)
                        .type("deposit")
                        .description("Nạp tiền khởi tạo số dư ví")
                        .build();
                allTransactions.add(clientDeposit);

                // Client hold
                WalletTransaction clientHold = WalletTransaction.builder()
                        .user(client)
                        .amount(-price)
                        .type("escrow_hold")
                        .reference(commission)
                        .description("Tạm khóa cọc commission #" + commission.getId())
                        .build();
                allTransactions.add(clientHold);

                // Client release
                WalletTransaction clientRelease = WalletTransaction.builder()
                        .user(client)
                        .amount(price)
                        .type("escrow_release")
                        .reference(commission)
                        .description("Giải ngân thanh toán commission #" + commission.getId())
                        .build();
                allTransactions.add(clientRelease);

                // Artist receive
                WalletTransaction artistReceive = WalletTransaction.builder()
                        .user(artist)
                        .amount(price)
                        .type("escrow_release")
                        .reference(commission)
                        .description("Nhận tiền hoàn thành commission #" + commission.getId())
                        .build();
                allTransactions.add(artistReceive);
            } else if ("refunded".equals(paymentStatus)) {
                WalletTransaction clientHold = WalletTransaction.builder()
                        .user(client)
                        .amount(-price)
                        .type("escrow_hold")
                        .reference(commission)
                        .description("Tạm khóa cọc commission #" + commission.getId())
                        .build();
                allTransactions.add(clientHold);

                WalletTransaction clientRefund = WalletTransaction.builder()
                        .user(client)
                        .amount(price)
                        .type("escrow_refund")
                        .reference(commission)
                        .description("Hoàn tiền cọc do hủy commission #" + commission.getId())
                        .build();
                allTransactions.add(clientRefund);
            }
        }
        walletTransactionRepository.saveAll(allTransactions);

        // --- 8. GENERATE MESSAGES (Chat History) ---
        List<Message> allMessages = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            User sender = allUsers.get(random.nextInt(allUsers.size()));
            User receiver = allUsers.get(random.nextInt(allUsers.size()));
            if (sender.getId().equals(receiver.getId())) continue;

            for (int m = 0; m < MESSAGE_TEXTS.length; m++) {
                User msgSender = (m % 2 == 0) ? sender : receiver;
                User msgReceiver = (m % 2 == 0) ? receiver : sender;

                Message message = Message.builder()
                        .sender(msgSender)
                        .receiver(msgReceiver)
                        .content(MESSAGE_TEXTS[m])
                        .isRead(m < MESSAGE_TEXTS.length - 1)
                        .build();
                allMessages.add(message);
            }
        }
        messageRepository.saveAll(allMessages);

        // --- 9. GENERATE NOTIFICATIONS ---
        List<Notification> allNotifications = new ArrayList<>();

        // Notifications for Follows
        for (int i = 0; i < Math.min(20, allFollows.size()); i++) {
            Follow f = allFollows.get(i);
            Notification notification = Notification.builder()
                    .recipient(f.getFollowing())
                    .actor(f.getFollower())
                    .type("follow")
                    .contentPreview(f.getFollower().getNickname() + " has started following you")
                    .isRead(random.nextBoolean())
                    .build();
            allNotifications.add(notification);
        }

        // Notifications for Likes
        for (int i = 0; i < Math.min(20, allLikes.size()); i++) {
            Like l = allLikes.get(i);
            Notification notification = Notification.builder()
                    .recipient(l.getIllustration().getArtist())
                    .actor(l.getUser())
                    .type("like")
                    .targetId(l.getIllustration().getId())
                    .targetModel("Illustration")
                    .contentPreview(l.getUser().getNickname() + " liked your artwork '" + l.getIllustration().getTitle() + "'")
                    .isRead(random.nextBoolean())
                    .build();
            allNotifications.add(notification);
        }

        // Notifications for Commissions
        for (int i = 0; i < Math.min(10, allCommissions.size()); i++) {
            Commission c = allCommissions.get(i);
            Notification notification = Notification.builder()
                    .recipient(c.getArtist())
                    .actor(c.getClient())
                    .type("commission_update")
                    .targetId(c.getId())
                    .targetModel("Commission")
                    .contentPreview("You have a new commission request from " + c.getClient().getNickname())
                    .isRead(random.nextBoolean())
                    .build();
            allNotifications.add(notification);
        }
        notificationRepository.saveAll(allNotifications);

        // --- 10. RECALCULATE & UPDATE USERS STATISTICS ---
        for (User user : allUsers) {
            int totalViews = 0;
            int totalLikes = 0;
            int totalBookmarks = 0;
            int totalComments = 0;

            // Get illustrations by this user
            List<Illustration> userIllustrations = illustrationRepository.findByArtistIdOrderByCreatedAtDesc(user.getId());
            for (Illustration ill : userIllustrations) {
                totalViews += ill.getViewsCount();
                totalLikes += ill.getLikesCount();
                totalBookmarks += ill.getBookmarksCount();
                totalComments += ill.getCommentsCount();
            }

            user.setTotalViews(totalViews);
            user.setTotalLikes(totalLikes);
            user.setTotalBookmarks(totalBookmarks);
            user.setTotalComments(totalComments);
            userRepository.save(user);
        }

        System.out.println("[DatabaseSeeder] Expanded database seeding completed successfully!");
        System.out.println("[DatabaseSeeder] Total Users: " + userRepository.count());
        System.out.println("[DatabaseSeeder] Total Illustrations: " + illustrationRepository.count());
        System.out.println("[DatabaseSeeder] Total Comments: " + commentRepository.count());
        System.out.println("[DatabaseSeeder] Total Likes: " + likeRepository.count());
        System.out.println("[DatabaseSeeder] Total Bookmarks: " + bookmarkRepository.count());
        System.out.println("[DatabaseSeeder] Total Follows: " + followRepository.count());
        System.out.println("[DatabaseSeeder] Total Commissions: " + commissionRepository.count());
    }
}
