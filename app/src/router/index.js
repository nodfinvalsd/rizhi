import { createRouter, createWebHashHistory } from 'vue-router'
import ListView from '../views/ListView.vue'
import EditView from '../views/EditView.vue'
import DetailView from '../views/DetailView.vue'
import ScheduleView from '../views/ScheduleView.vue'
import SummaryView from '../views/SummaryView.vue'
import WidgetApp from '../widget/WidgetApp.vue'

export default createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: '/', component: ListView },
    { path: '/edit/:id?', component: EditView },
    { path: '/detail/:id', component: DetailView },
    { path: '/schedule', component: ScheduleView },
    { path: '/summary', component: SummaryView },
    { path: '/widget', component: WidgetApp },
  ],
})
