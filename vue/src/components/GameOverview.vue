<template>

          <!-- <tr class = "game-info" > -->
            <tr>
            <!-- <router-link :to="" -->
            <router-link :to=" {name: 'game-details', params: {id: userGame.id} }">
            <td>{{userGame.gameName}}</td> 
             </router-link>
            <td>{{userGame.startingAmount}}</td> 
            <td>{{userGame.endDate}}</td>
           
            <td>
                <button class="invite-btn" href="#invite-form" v-on:click.prevent="showForm = !showForm" v-show="showForm==false">Invite Player</button>
                 <form id="invite-form" v-on:submit.prevent v-show="showForm == true">
                    <label id="player-info" for="receiver-name">Enter Player's Username</label>
                    <input id="receiver-name" type="text" v-model="receiver.receiverName" />
                    <input type="submit" value="Save" v-on:click="inviteToGame(userGame.id)">
                    <input type="button" value="Cancel" v-on:click="resetForm">
                </form>        
            </td>
           
          </tr>
   
  
</template>

<script>

import gameService from "../services/GameService";
import authService from '../services/AuthService';

export default {
    name: 'game-overview',
    props: ['userGame'],
    showForm: false,
    data() {
        return{
            game: {
                gameId: '',
                gameName: '',
                startingAmount: '',
                endDate: ''
            },
            receiver: {
                receiverName: ''
            },          
            inviteType: {
                senderId: '',
                receiverId: '',
                gameId: '',
                gameInviteTypeId: 3

            },
            showForm: false,
            showPending: false
        }
    }, methods: {
            inviteToGame(gameId) {
                authService.getGamerByUsername(this.receiver.receiverName).then( response =>{
                this.inviteType.receiverId = response.userId;
                this.inviteType.senderId = this.$store.state.user.userId;
                this.inviteType.gameId = gameId;
                
                        gameService.inviteToGame(this.inviteType).then( response => {
                                if(response.status == 200) {
                                    this.resetForm();
                                }
                            }).catch()
                })
            },
            resetForm() {
                this.receiver.receiverName = '';
                this.showForm = false;
            },
            setForm(){
                authService.getGamerByUsername(this.receiver.receiverName).then( response =>{
                this.inviteType.receiverId = response.userId;
                return this.inviteType;
                })
            }
    }

}
</script>

<style>
#invite-form{
    width: 100%;
    text-align: center;
}

#player-info {
    color: white;
}
.invite-btn{
    width: 118px;
}

/* .pendingInvites{
    display: flex;
    justify-content: center;
}

.userGames {
    display: flex;
    flex-direction: column;
    border:1px solid black;
   
}
.game-header{
    display:flex;
	background: rgba(4, 42, 61) 0%;
    color: #c99200;
    justify-content: space-evenly;
}

.game-info{
    display:flex;
    justify-content: space-evenly;
}


.game-data{
    border-bottom: 2px solid rgba(4, 42, 61) 0%;
}
.td-left{
    text-align: left;
}
.td-right{
    text-align: right;
}

th,td{
    padding: 20px;   
}

tr.game-info:hover {
    background: rgba(4, 42, 61) 0%;
    color: #c99200;
}

#invite-form:hover {
    text-decoration: #c99200;
} */
</style>