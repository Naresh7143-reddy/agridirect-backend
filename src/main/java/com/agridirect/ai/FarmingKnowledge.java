package com.agridirect.ai;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Built-in farming knowledge base used as a fallback when the Gemini API call
 * fails (rate limit, invalid key, network, etc). Keyword-matches the user's
 * question and returns a sensible, India-focused answer.
 *
 * The goal isn't to replace the LLM — it's to guarantee the AI Assistant
 * always returns SOMETHING useful even when Gemini is unavailable.
 */
public final class FarmingKnowledge {

    private FarmingKnowledge() {}

    private static final List<Entry> ENTRIES = List.of(
        new Entry(
            List.of("price", "sell", "market", "profit", "rate", "best time", "when to sell"),
            "💰 *Best time to sell for maximum profit*\n\n" +
            "1. Track your local mandi (APMC) prices on the **eNAM portal** or **Kisan Suvidha app** for 7 days before selling.\n" +
            "2. Avoid selling immediately after harvest — prices are usually lowest then due to supply glut.\n" +
            "3. The sweet spot is typically 2–3 weeks after peak harvest, when supply tightens.\n" +
            "4. For storable crops (onion, potato, garlic, pulses) — store in proper conditions and watch for festival demand (Diwali, Pongal).\n" +
            "5. Sell directly to buyers through AgriDirect — you'll get 20–30% better prices than mandi rates by cutting out the middleman.\n\n" +
            "📈 Tip: Check Agmarknet.gov.in for real-time price trends in your state."
        ),
        new Entry(
            List.of("disease", "pest", "insect", "spot", "leaf", "wilting", "yellow", "fungal"),
            "🌱 *Crop disease & pest management*\n\n" +
            "**Quick triage:**\n" +
            "• Yellow leaves with veins still green → nitrogen deficiency (apply urea / FYM)\n" +
            "• Brown/black spots on leaves → fungal infection (spray neem oil 5ml/L or copper oxychloride)\n" +
            "• Sticky residue + ants → aphids (spray 5% neem oil or imidacloprid)\n" +
            "• White powder on leaves → powdery mildew (sulfur dust 25g/L)\n" +
            "• Holes in leaves → caterpillars / beetles (Bt or quinalphos)\n\n" +
            "**Prevention is cheaper than cure:**\n" +
            "1. Use disease-resistant seed varieties\n" +
            "2. Rotate crops every season\n" +
            "3. Keep field free of weeds and crop residue\n" +
            "4. Don't over-water — most fungi need moisture\n\n" +
            "📸 You can also upload a photo using the camera button — I'll identify the exact issue."
        ),
        new Entry(
            List.of("fertilizer", "manure", "npk", "urea", "dap", "nutrient", "feeding"),
            "🌾 *Fertilizer & nutrient guide*\n\n" +
            "**Basic NPK ratio for common crops:**\n" +
            "• Rice (paddy): 120-60-40 kg/ha (N-P-K)\n" +
            "• Wheat: 120-60-40 kg/ha\n" +
            "• Maize: 150-75-50 kg/ha\n" +
            "• Cotton: 100-50-50 kg/ha\n" +
            "• Tomato: 100-50-50 kg/ha\n\n" +
            "**Application schedule:**\n" +
            "1. **Basal dose** (at sowing): All P + K + 50% N\n" +
            "2. **First top dressing** (25–30 days): 25% N\n" +
            "3. **Second top dressing** (45–60 days): 25% N\n\n" +
            "**Organic alternatives:** FYM 10 t/ha + vermicompost 2 t/ha + jeevamrut spray. Reduces chemical cost by 40%.\n\n" +
            "💡 Soil test every 3 years (free at KVK) to apply only what's needed — saves money + boosts yield."
        ),
        new Entry(
            List.of("irrigation", "water", "drip", "watering", "moisture"),
            "💧 *Water management*\n\n" +
            "**Best practices:**\n" +
            "1. **Drip irrigation** saves 40–60% water vs. flooding — get up to 90% subsidy under PMKSY scheme\n" +
            "2. Water early morning (5–8 AM) or evening (5–7 PM) — reduces evaporation\n" +
            "3. Check soil moisture by squeeze test — if soil holds shape, no need to water\n" +
            "4. **Mulching** (with straw/plastic) cuts watering frequency in half\n\n" +
            "**Critical irrigation stages:**\n" +
            "• Rice: tillering, flowering, grain filling\n" +
            "• Wheat: crown root initiation (21 DAS), flowering, grain filling\n" +
            "• Tomato: fruit setting, fruit development\n\n" +
            "Apply for **PM Kisan Sinchayee Yojana** at your nearest agriculture office for drip system subsidy."
        ),
        new Entry(
            List.of("weather", "rain", "monsoon", "drought", "season", "kharif", "rabi", "zaid"),
            "☀️ *Weather & seasonal planning*\n\n" +
            "**Indian growing seasons:**\n" +
            "• **Kharif** (Jun–Oct): Rice, cotton, maize, soybean, groundnut, sugarcane — sown with monsoon\n" +
            "• **Rabi** (Oct–Mar): Wheat, mustard, gram, peas, barley — depend on winter moisture\n" +
            "• **Zaid** (Mar–Jun): Watermelon, muskmelon, vegetables — short summer season\n\n" +
            "**Daily checks:**\n" +
            "1. Get forecasts on **Meghdoot app** (IMD official)\n" +
            "2. **Damini app** alerts for lightning — save lives during fieldwork\n" +
            "3. If heavy rain forecast: postpone fertilizer/pesticide spraying by 48 hours\n" +
            "4. Pre-monsoon: ensure drainage; post-monsoon: drain excess water within 24 hours\n\n" +
            "🌧️ El Niño years often bring weaker monsoon — switch to drought-resistant varieties."
        ),
        new Entry(
            List.of("scheme", "subsidy", "loan", "government", "pm kisan", "kisaan credit", "kcc", "insurance"),
            "🏛️ *Government schemes for farmers*\n\n" +
            "**Direct income support:**\n" +
            "• **PM-KISAN**: ₹6,000/year in 3 installments → register at pmkisan.gov.in with Aadhaar\n" +
            "• **PM Kisan Maandhan**: Pension scheme — ₹3,000/month after age 60\n\n" +
            "**Credit:**\n" +
            "• **Kisan Credit Card (KCC)**: ₹3 lakh @ 7% interest, often reduced to 4% with timely repayment\n" +
            "• **Agri Infra Fund**: Up to ₹2 crore for cold storage, processing units (3% interest subvention)\n\n" +
            "**Insurance:**\n" +
            "• **PMFBY** (Pradhan Mantri Fasal Bima Yojana): premium 1.5–2% of sum insured. Covers natural calamities, pests, diseases.\n\n" +
            "**Equipment subsidy:**\n" +
            "• **SMAM**: 40–50% subsidy on tractors, harvesters, sprayers\n" +
            "• **PMKSY**: 90% subsidy on drip/sprinkler systems for small farmers\n\n" +
            "📞 Helpline: **Kisan Call Centre 1800-180-1551**"
        ),
        new Entry(
            List.of("organic", "natural farming", "vermicompost", "jeevamrut", "panchagavya"),
            "🌿 *Organic & natural farming*\n\n" +
            "**Why switch:**\n" +
            "• Premium price — organic produce sells 30–50% higher\n" +
            "• Lower input costs (no chemicals)\n" +
            "• Better soil health long-term\n\n" +
            "**Starter kit (do these first):**\n" +
            "1. **Vermicompost** pit: 1 m × 1 m, mix cow dung + leaves + worms. Ready in 60 days.\n" +
            "2. **Jeevamrut**: 10 kg cow dung + 10 L cow urine + 2 kg jaggery + 2 kg gram flour + 200 L water. Ferment 7 days, spray weekly.\n" +
            "3. **Panchagavya**: 5 kg cow dung + 1 L ghee + 3 L cow urine + 2 L milk + 2 L curd + 3 L tender coconut water + 12 bananas. Spray as growth booster.\n" +
            "4. **Neem-based pest spray**: 5 kg neem leaves boiled in 20 L water — natural insecticide.\n\n" +
            "**Certification:**\n" +
            "• **PGS-India** (Participatory Guarantee Scheme): free certification for small farmers via NGOs\n" +
            "• **NPOP**: paid certification needed for export\n\n" +
            "Transition takes 2–3 seasons. Yield drops 10–20% in year 1, recovers and exceeds by year 3."
        ),
        new Entry(
            List.of("tomato", "potato", "onion", "wheat", "rice", "paddy", "cotton", "sugarcane", "maize", "chilli", "groundnut"),
            "🌾 *Crop-specific guidance*\n\n" +
            "Please ask a more specific question like:\n" +
            "• \"How to grow tomato in kharif?\"\n" +
            "• \"Best fertilizer for wheat?\"\n" +
            "• \"Tomato leaf disease — what should I do?\"\n" +
            "• \"When to harvest paddy?\"\n\n" +
            "Or tap the 📷 camera button to upload a photo of your crop for instant disease detection.\n\n" +
            "I can help with sowing, watering, fertilizers, pests, harvest timing, and market prices for all major Indian crops."
        ),
        new Entry(
            List.of("hello", "hi", "namaste", "hey", "namaskaram"),
            "🙏 Namaste! I'm Krishi AI, your farming assistant.\n\n" +
            "I can help you with:\n" +
            "• 🌱 Crop diseases & pest control\n" +
            "• 🌾 Fertilizer schedules\n" +
            "• 💧 Irrigation tips\n" +
            "• 💰 Market prices & best time to sell\n" +
            "• 🏛️ Government schemes (PM-KISAN, KCC, PMFBY)\n" +
            "• 🌿 Organic farming methods\n" +
            "• ☀️ Weather-based decisions\n\n" +
            "Ask me anything in English, Hindi, Telugu, or Tamil. You can also send a photo of your crop for disease detection.\n\n" +
            "What would you like to know today?"
        )
    );

    private static final String DEFAULT_REPLY =
        "🙏 I'd love to help! Could you rephrase your question?\n\n" +
        "I can answer questions about:\n" +
        "• Crop diseases (\"my tomato leaves have brown spots\")\n" +
        "• Fertilizer (\"how much urea for wheat\")\n" +
        "• Market prices (\"when to sell onion for best price\")\n" +
        "• Government schemes (\"how to apply PM-KISAN\")\n" +
        "• Weather & seasons (\"best crop for kharif\")\n" +
        "• Organic farming (\"how to make jeevamrut\")\n\n" +
        "📸 You can also tap the camera button to upload a crop photo for instant disease detection.";

    /** Looks up the best matching reply for the given user message. */
    public static String findReply(String message) {
        if (message == null || message.isBlank()) return DEFAULT_REPLY;
        String lower = message.toLowerCase(Locale.ROOT);
        int bestScore = 0;
        String bestReply = DEFAULT_REPLY;
        for (Entry e : ENTRIES) {
            int score = 0;
            for (String kw : e.keywords) {
                if (lower.contains(kw)) score++;
            }
            if (score > bestScore) {
                bestScore = score;
                bestReply = e.reply;
            }
        }
        return bestReply;
    }

    /** Crop-advice fallback for /crop-advice endpoint. */
    public static String cropAdviceFallback(String season, String location, String soilType, String waterAvailability) {
        return "🌾 *Recommended crops for " + safe(location) + " (" + safe(season) + " season, " + safe(soilType) + " soil)*\n\n" +
            "Based on standard agro-climatic guidance for India:\n\n" +
            "**1. Rice (Paddy)** 🌾\n" +
            "• Best in: Kharif, clay/loamy soil, abundant water\n" +
            "• Yield: 4–6 tonnes/ha\n" +
            "• Market price: ₹20–28/kg\n" +
            "• Demand: High (FCI procurement)\n" +
            "• Tip: Use SRI method for 30% higher yield with less water\n\n" +
            "**2. Pulses (Tur/Moong)** 🫘\n" +
            "• Best in: Rabi/Kharif, well-drained soil, low water\n" +
            "• Yield: 1–1.5 tonnes/ha\n" +
            "• Market price: ₹70–120/kg\n" +
            "• Demand: Very high (import-dependent)\n" +
            "• Tip: MSP support guaranteed; fixes nitrogen for next crop\n\n" +
            "**3. Vegetables (Tomato/Onion)** 🍅\n" +
            "• Best in: All seasons with irrigation\n" +
            "• Yield: 20–40 tonnes/ha\n" +
            "• Market price: ₹15–80/kg (volatile)\n" +
            "• Demand: Constant, urban-focused\n" +
            "• Tip: Stagger planting in batches to avoid market glut\n\n" +
            "💡 Run a soil test at your nearest KVK (free) for crop-specific recommendations.";
    }

    /** Price-forecast fallback. */
    public static String priceForecastFallback(String cropName, String location) {
        String crop = safe(cropName);
        return "📊 *" + crop.toUpperCase() + " market outlook for " + safe(location) + "*\n\n" +
            "**Current price range:** Check live rates on **eNAM** (enam.gov.in) or **Agmarknet** for today's mandi price.\n\n" +
            "**Price trend (general guidance):**\n" +
            "• Prices typically dip 20–30% right after harvest due to supply glut\n" +
            "• Recover and rise over next 6–8 weeks as supply tightens\n" +
            "• Festival months (Oct–Nov, Jan) often see 10–15% spike for vegetables\n\n" +
            "**Best time to sell:**\n" +
            "1. **Avoid** selling in the first 2 weeks post-harvest\n" +
            "2. **Wait 3–4 weeks** if you have proper storage\n" +
            "3. **Hold for festival season** if " + crop.toLowerCase() + " is storable\n\n" +
            "**Nearby markets to compare:**\n" +
            "• Check 3–4 mandis within 100 km — prices often vary by ₹2–5/kg\n" +
            "• Use the **eNAM mobile app** to bid across India\n" +
            "• Sell directly via AgriDirect for 20–30% better prices vs mandi\n\n" +
            "**Tips to maximise:**\n" +
            "• Grade your produce (A, B, C) — top grade sells 40% higher\n" +
            "• Clean packaging adds 10–15% premium\n" +
            "• Build buyer contacts over time (hotels, retailers, AgriDirect buyers)";
    }

    private static String safe(String s) { return s == null || s.isBlank() ? "—" : s; }

    private record Entry(List<String> keywords, String reply) {}
}
