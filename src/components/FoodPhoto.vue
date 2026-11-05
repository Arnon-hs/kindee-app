<script setup lang="ts">
import {computed,ref,watch} from 'vue';
import {useI18n} from 'vue-i18n';
import {photoFor,photoUrl} from '../photos';
const props=defineProps<{slug:string;alt:string;compact?:boolean}>();const {t}=useI18n();const failed=ref(false);
const photo=computed(()=>photoFor(props.slug));const full=computed(()=>photo.value?.variants.find(v=>v.filename===photo.value?.full));
watch(()=>props.slug,()=>{failed.value=false;});
</script>
<template><figure class="food-photo" :class="{'food-photo-compact':compact}"><img v-if="photo&&!failed" :src="photoUrl(compact?photo.thumbnail:photo.full)" :srcset="compact?undefined:photo.variants.map(v=>`${photoUrl(v.filename)} ${v.width}w`).join(', ')" sizes="(max-width:700px) 100vw, 500px" :width="full?.width" :height="full?.height" :alt="alt+' · '+t('photoIllustrative')" loading="lazy" decoding="async" @error="failed=true"><span v-else class="photo-fallback" role="img" :aria-label="t('photoUnavailable')">{{t('photoUnavailable')}}</span><figcaption v-if="photo&&!compact"><p>{{t('photoIllustrative')}}</p><small>{{t('photoCredit')}}: <a :href="photo.source_page" target="_blank" rel="noopener noreferrer">{{photo.author}}</a> · <a :href="photo.license_url" target="_blank" rel="license noopener noreferrer">{{photo.license}}</a></small></figcaption></figure></template>
