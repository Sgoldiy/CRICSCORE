# AdMob Test Ad Unit Reference

## ⚠️ CRITICAL: Ad Unit Format Compatibility

The error **"Ad unit doesn't match format"** occurs when you use an ad unit designed for one format (e.g., Native) with a different format (e.g., Banner).

---

## 📋 Google's Official Test Ad Units

### ✅ **BANNER Ad Test Units** (Use these for `AdView`)

| Ad Size | Test Ad Unit ID | Status |
|---------|----------------|--------|
| **Banner** | `ca-app-pub-3940256099942544/6300978111` | ✅ WORKS |
| Banner | `ca-app-pub-3940256099942544/9214589741` | ✅ WORKS |

**Usage in Firebase:**
```json
{
  "slider_big_native": {
    "ads_unit": "ca-app-pub-3940256099942544/6300978111",
    "enabled": true
  }
}
```

---

### ❌ **NATIVE ADVANCED Test Units** (DO NOT use with `AdView`)

| Ad Type | Test Ad Unit ID | Compatible With |
|---------|----------------|-----------------|
| Native Advanced | `ca-app-pub-3940256099942544/2247696110` | ❌ NativeAdView ONLY |

**This is causing your current error!** You're using this Native Advanced unit with a standard `AdView`, which only supports Banner ads.

---

## 🔍 Understanding Your Error

```
Ad failed to load: Ad unit doesn't match format, Code: 3
```

**What this means:**
- You're trying to load a **Native Advanced ad unit** (`.../2247696110`) 
- Using a standard **AdView** component (which only shows Banner ads)
- The formats don't match = **Error Code 3**

**Solution:**
Change your Firebase config to use the BANNER test unit: `ca-app-pub-3940256099942544/6300978111`

---

## 🛠️ Implementation Guide

### For Your App (Current Implementation)

Your `NativeAdViewHorizontal` uses `AdView` with `AdSize.LARGE_BANNER`:

```kotlin
AndroidView(
    factory = { context ->
        AdView(context).apply {
            setAdSize(AdSize.LARGE_BANNER) // 320x100
            setAdUnitId(adUnitId) // Must be BANNER format unit!
        }
    }
)
```

**✅ CORRECT Firebase Config:**
```json
{
  "slider_big_native": {
    "ads_unit": "ca-app-pub-3940256099942544/6300978111",
    "enabled": true
  }
}
```

**❌ WRONG Firebase Config (Current):**
```json
{
  "slider_big_native": {
    "ads_unit": "ca-app-pub-3940256099942544/2247696110",
    "enabled": true
  }
}
```

---

## 📊 All AdMob Test Ad Units Reference

### Banner Ads
```
ca-app-pub-3940256099942544/6300978111  // Banner - Standard test
ca-app-pub-3940256099942544/9214589741  // Banner - Alternate
```

### Interstitial Ads
```
ca-app-pub-3940256099942544/1033173712  // Interstitial - Full screen
```

### Rewarded Ads
```
ca-app-pub-3940256099942544/5224354917  // Rewarded - Video reward
```

### Native Advanced Ads
```
ca-app-pub-3940256099942544/2247696110  // Native Advanced - Requires NativeAdView
ca-app-pub-3940256099942544/3986624511  // Native Advanced - Video
```

### App Open Ads
```
ca-app-pub-3940256099942544/3419835294  // App Open - Cold start
ca-app-pub-3940256099942544/9257396501  // App Open - Warm start
```

---

## ✅ Quick Fix Steps

1. **Open Firebase Console** → Realtime Database
2. **Navigate to:** `ads_config` → `slider_big_native`
3. **Change:** `ads_unit` value from:
   ```
   ca-app-pub-3940256099942544/2247696110  // ❌ WRONG
   ```
   To:
   ```
   ca-app-pub-3940256099942544/6300978111  // ✅ CORRECT
   ```
4. **Save** the changes
5. **Restart your app** - The ad should now load successfully!

---

## 🎯 Expected Result After Fix

**Before (Current Error):**
```
D/NativeAdViewHorizontal: Loading ad from unit: ca-app-pub-3940256099942544/2247696110
E/NativeAdViewHorizontal: Ad failed to load: Ad unit doesn't match format, Code: 3
```

**After (Success):**
```
D/NativeAdViewHorizontal: Loading ad from unit: ca-app-pub-3940256099942544/6300978111
D/NativeAdViewHorizontal: ✅ Ad loaded successfully
```

---

## 📱 Production Deployment

When ready for production, replace test units with YOUR ad units:

```json
{
  "slider_big_native": {
    "ads_unit": "ca-app-pub-YOUR_PUB_ID/YOUR_AD_UNIT_ID",
    "enabled": true
  }
}
```

⚠️ **Warning:** Never use production ad units during development without testing first!

---

## 🔗 References

- [AdMob Test Ad Units](https://support.google.com/admob/answer/9905175)
- [Banner Ads Guide](https://developers.google.com/admob/android/banner)
- [Native Ads Guide](https://developers.google.com/admob/android/native/start)
