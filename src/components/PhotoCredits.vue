<script setup lang="ts">
import {useI18n} from 'vue-i18n';
import {photos} from '../photos';
const {t}=useI18n();
</script>
<template><details class="photo-credits"><summary>{{t('licenses')}}</summary><p>{{t('photoIllustrative')}}</p><ul><li v-for="photo in photos" :key="photo.id"><a :href="photo.source_page" target="_blank" rel="noopener noreferrer">{{photo.author}}</a> · <a :href="photo.license_url" target="_blank" rel="license noopener noreferrer">{{photo.license}}</a><small>{{photo.title}} — {{photo.changes}}</small></li></ul></details></template>
